package io.github.datallmhub.agentflow4j.graph;

import java.util.Optional;

public interface CheckpointStore {

    void save(Checkpoint checkpoint);

    Optional<Checkpoint> load(String runId);

    void delete(String runId);
}
