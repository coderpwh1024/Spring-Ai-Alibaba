package com.coderpwh.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphStateException;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/analyze")
public class ParallelController {


    private final CompiledGraph engine;

    public ParallelController(StateGraph parallelGraph) throws GraphStateException {
        SaverConfig saverConfig = SaverConfig.builder().build();
        this.engine = parallelGraph.compile(CompileConfig.builder().saverConfig(saverConfig).interruptBefore("merge").build());
    }

    @GetMapping
    public Map<String, Object> analyze(@RequestParam String text) {
        return engine.invoke(Map.of("inputText", text)).get().data();
    }

    @GetMapping(path = "/stream", produces = "text/event-stream")
    public Flux<Map<String, Object>> analyzeStream(@RequestParam String text) {
        RunnableConfig cfg = RunnableConfig.builder().streamMode(CompiledGraph.StreamMode.SNAPSHOTS).build();
        return Flux.create(sink -> {
            engine.stream(Map.of("inputText", text), cfg)
                    .forEachAsync(node -> sink.next(node.state().data()))
                    .whenComplete((v, e) -> {
                        if (e != null) {
                            sink.error(e);
                        } else {
                            sink.complete();
                        }
                    });
        });
    }
}
