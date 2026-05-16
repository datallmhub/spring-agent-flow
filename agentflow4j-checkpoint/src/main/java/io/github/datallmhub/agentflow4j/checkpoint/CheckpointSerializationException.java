package io.github.datallmhub.agentflow4j.checkpoint;

public class CheckpointSerializationException extends RuntimeException {

    public CheckpointSerializationException(String message) {
        super(message);
    }

    public CheckpointSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
