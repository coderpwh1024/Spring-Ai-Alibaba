package com.coderpwh.tools;

import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

/**
 * @author coderpwh
 */
public interface Tool<I, O> extends BiFunction<I, ToolContext, O> {


}
