package com.coderpwh.tool.support;

/**
 * @author coderpwh
 */
public class ExecuteCommandResult {

    private String output;

    private Integer exitCode;

    String getOutput() {
        return output;
    }

    void setOutput(String output) {
        this.output = output;
    }

    Integer getExitCode() {
        return exitCode;
    }

    void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

}
