package io.github.datallmhub.agentflow4j.playground;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.datallmhub.agentflow4j.core.Agent;
import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentEvent;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.Disposable;

@Controller
@RequestMapping("/playground")
class PlaygroundController {

    private static final long EMITTER_TIMEOUT_MS = 60_000L;

    private final Map<String, Agent> agents;
    private final PlaygroundProperties properties;

    PlaygroundController(Map<String, Agent> agents, PlaygroundProperties properties) {
        this.agents = Objects.requireNonNull(agents, "agents");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @GetMapping
    String index(Model model) {
        List<String> agentNames = agents.keySet().stream().sorted().toList();
        model.addAttribute("title", properties.getTitle());
        model.addAttribute("agents", agentNames);
        return "playground/index";
    }

    @GetMapping(path = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter chat(@RequestParam("agent") String agentName,
                    @RequestParam("message") String message,
                    HttpSession httpSession) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        Agent agent = agents.get(agentName);
        if (agent == null) {
            emitTerminal(emitter, "error", "unknown agent: " + agentName);
            return emitter;
        }
        if (message == null || message.isBlank()) {
            emitTerminal(emitter, "error", "message must not be blank");
            return emitter;
        }

        PlaygroundSession session = playgroundSession(httpSession);
        AgentContext context;
        synchronized (session) {
            context = session.contextFor(agentName).withMessage(new UserMessage(message));
            session.update(agentName, context);
        }

        Disposable subscription = agent.executeStream(context).subscribe(
                event -> dispatch(emitter, event, agentName, session),
                throwable -> emitTerminal(emitter, "error",
                        throwable.getMessage() != null ? throwable.getMessage() : "stream error"),
                emitter::complete);
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(subscription::dispose);
        return emitter;
    }

    @PostMapping("/api/reset")
    RedirectView reset(@RequestParam(value = "agent", required = false) String agentName,
                       HttpSession httpSession) {
        PlaygroundSession session = playgroundSession(httpSession);
        synchronized (session) {
            if (agentName == null || agentName.isBlank()) {
                session.resetAll();
            } else {
                session.reset(agentName);
            }
        }
        return new RedirectView("/playground");
    }

    private void dispatch(SseEmitter emitter, AgentEvent event,
                          String agentName, PlaygroundSession session) {
        try {
            if (event instanceof AgentEvent.Token token) {
                send(emitter, "token", token.chunk());
            }
            else if (event instanceof AgentEvent.NodeTransition t) {
                send(emitter, "transition", t.from() + "→" + t.to());
            }
            else if (event instanceof AgentEvent.ToolCallStart start) {
                send(emitter, "tool", start.toolName());
            }
            else if (event instanceof AgentEvent.ToolCallEnd end) {
                send(emitter, "tool-end", end.record().name());
            }
            else if (event instanceof AgentEvent.Completed completed) {
                onCompleted(emitter, completed.result(), agentName, session);
            }
        }
        catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }

    private void onCompleted(SseEmitter emitter, AgentResult result,
                             String agentName, PlaygroundSession session) throws IOException {
        if (result.hasError()) {
            String text = result.error().cause() != null
                    ? result.error().cause().getMessage()
                    : "agent failed";
            send(emitter, "error", text);
            return;
        }
        synchronized (session) {
            AgentContext current = session.contextFor(agentName);
            session.update(agentName, current.applyResult(result));
        }
        send(emitter, "completed", result.text() != null ? result.text() : "");
    }

    private void send(SseEmitter emitter, String name, String data) throws IOException {
        emitter.send(SseEmitter.event().name(name).data(data == null ? "" : data));
    }

    private void emitTerminal(SseEmitter emitter, String name, String data) {
        try {
            send(emitter, name, data);
        }
        catch (IOException ignored) {
            // client gone; nothing to do
        }
        emitter.complete();
    }

    private PlaygroundSession playgroundSession(HttpSession httpSession) {
        Object existing = httpSession.getAttribute(PlaygroundSession.SESSION_KEY);
        if (existing instanceof PlaygroundSession s) {
            return s;
        }
        PlaygroundSession s = new PlaygroundSession();
        httpSession.setAttribute(PlaygroundSession.SESSION_KEY, s);
        return s;
    }
}
