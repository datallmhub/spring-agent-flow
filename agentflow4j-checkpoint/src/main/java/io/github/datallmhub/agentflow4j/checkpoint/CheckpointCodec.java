package io.github.datallmhub.agentflow4j.checkpoint;

import io.github.datallmhub.agentflow4j.graph.Checkpoint;

public interface CheckpointCodec {

    String encode(Checkpoint checkpoint);

    Checkpoint decode(String payload);
}
