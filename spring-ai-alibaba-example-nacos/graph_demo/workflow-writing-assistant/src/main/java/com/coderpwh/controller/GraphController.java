package com.coderpwh.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {


    private final CompiledGraph graph;

    public GraphController(CompiledGraph graph) {
        this.graph = graph;
    }


    @PostMapping("/invoke")
    public ResponseEntity<Map<String, Object>> invoke(@RequestBody Map<String, Object> inputs) throws GraphStateException, GraphRunnerException {
        var resultFuture = graph.invoke(inputs);
        return ResponseEntity.ok(resultFuture.get().data());
    }


    @GetMapping(path="/mock/http")
    public String mock(@RequestParam("ticketId") String ticketId, @RequestParam("category")  String category){
        Map<String, String> resp = Map.of("status", "OK", "ticketId", ticketId, "category", category);
        return resp.toString();
    }


}
