package com.coderpwh.controller;

import com.coderpwh.flow.PlanningFlow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author coderpwh
 */

@RestController
@RequestMapping("/manus")
public class ManusController {

    private final PlanningFlow planningFlow;

    ManusController(PlanningFlow planningFlow) {
        this.planningFlow = planningFlow;
    }

    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
        planningFlow.setActivePlanId("plan_" + System.currentTimeMillis());
        return planningFlow.execute(query);
    }
}
