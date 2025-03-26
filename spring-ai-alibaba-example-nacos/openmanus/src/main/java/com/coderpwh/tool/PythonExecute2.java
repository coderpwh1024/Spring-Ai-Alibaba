package com.coderpwh.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.coderpwh.tool.support.CodeExecutionResult;
import com.coderpwh.tool.support.CodeUtils;
import com.coderpwh.tool.support.LogIdGenerator;
import com.coderpwh.tool.support.ToolExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * @author coderpwh
 */
public class PythonExecute2 implements Function<String, ToolExecuteResult> {

    private static final Logger log = LoggerFactory.getLogger(PythonExecute2.class);

    private Boolean arm64 = false;

    public static final String LLMMATH_PYTHON_CODE = "import sys; import math; import numpy as np; import numexpr as ne; input = '%s'; res = ne.evaluate(input); print(res)";

    private static String PARAMETERS = """
            {
                "type": "object",
                "properties": {
                    "code": {
                        "type": "string",
                        "description": "The Python code to execute."
                    }
                },
                "required": ["code"]
            }
            """;

    private static final String name = "python_execute";

    private static final String description = """
            Executes Python code string. Note: Only print outputs are visible, function return values are not captured. Use print statements to see results.
            """;

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback() {
        return FunctionToolCallback.builder(name, new PythonExecute2())
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("PythonExecute toolInput:" + toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {
        });
        String code = (String) toolInputMap.get("code");
        // String result = PythonUtils.invokePythonCodeWithArch(code, arm64);
        CodeExecutionResult codeExecutionResult = CodeUtils.executeCode(code, "python",
                "tmp_" + LogIdGenerator.generateUniqueId() + ".py", arm64, new HashMap<>());
        String result = codeExecutionResult.getLogs();
        return new ToolExecuteResult(result);
    }

    public static void main(String[] args) {

        String code = """
                import matplotlib.pyplot as plt
                import numpy as np

                # Fake data for demonstration purposes
                dates = np.arange("2023-03-01", "2023-03-08", dtype="datetime64[D]")
                prices = np.random.uniform(low=124.95, high=148.43, size=len(dates))

                plt.figure(figsize=(10, 5))
                plt.plot(dates, prices, marker='o')
                plt.title("Alibaba Stock Price Trend (Recent Week)")
                plt.xlabel("Date")
                plt.ylabel("Price in USD")
                plt.grid(True)
                plt.xticks(rotation=45)
                plt.tight_layout() # Adjust layout to fit everything nicely
                plt.show()
                """;
        PythonExecute2 pythonExecute = new PythonExecute2();
        // String toolInput = "print('hello')";
        String toolInput = String.format(LLMMATH_PYTHON_CODE, "2 + 3 * 5");
        ToolExecuteResult toolExecuteResult = pythonExecute.run(String.format("{\"code\":\"%s\"}", code));
        System.out.println(JSON.toJSON(toolExecuteResult));
    }

    public Boolean getArm64() {
        return arm64;
    }

    public void setArm64(Boolean arm64) {
        this.arm64 = arm64;
    }

    @Override
    public ToolExecuteResult apply(String s) {
        return run(s);
    }

}
