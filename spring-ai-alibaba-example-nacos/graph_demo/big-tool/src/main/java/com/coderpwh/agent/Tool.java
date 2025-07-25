package com.coderpwh.agent;

import java.util.function.Function;

public class Tool {

    private String name;


    private String description;

    private Function<Object[], Object> function;

    private Class<?>[] paramTypes;

    public Tool(String name, String description, Function<Object[], Object> function, Class<?>[] paramTypes) {
        this.name = name;
        this.description = description;
        this.function = function;
        this.paramTypes = paramTypes;
    }


    public Object execute(Object... args) {
        return function.apply(args);
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Function<Object[], Object> getFunction() {
        return function;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    @Override
    public String toString() {
        return "Tool{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
