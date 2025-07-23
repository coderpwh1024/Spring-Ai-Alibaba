package com.coderpwh.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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


    private final CompiledGraph compiledGraph;


    @Autowired
    public ParallelController(@Qualifier("parallelGraph") StateGraph parallelGraph) throws Exception {
        SaverConfig saverConfig = SaverConfig.builder().build();
        this.compiledGraph = parallelGraph.compile(CompileConfig.builder().saverConfig(saverConfig).interruptBefore("merge").build());
    }

    @GetMapping
    public Map<String, Object> analyze(@RequestParam("text") String text) throws GraphRunnerException {
        return compiledGraph.invoke(Map.of("text", text)).get().data();
    }


    @GetMapping(path = "/stream", produces = "text/event-stream")
    public Flux<Map<String, Object>> analyzeStream(@RequestParam("text") String text) {
        RunnableConfig cfg = RunnableConfig.builder().streamMode(CompiledGraph.StreamMode.SNAPSHOTS).build();

        return Flux.create(sink -> {
            try {
                compiledGraph.stream(Map.of("inputText", text), cfg)
                        .forEachAsync(node -> sink.next(node.state().data()))
                        .whenComplete((v, e) -> {
                            if (e != null) {
                                sink.error(e);
                            } else {
                                sink.complete();
                            }
                        });
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }
        });

    }


}
