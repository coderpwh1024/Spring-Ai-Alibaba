package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coderpwh
 */
@SuppressWarnings("null")
public class EvaluatorOptimizer {

    /**
     * 生成部分提示词
     */
    public static final String DEFAULT_GENERATOR_PROMPT = """
            你的目标是根据输入完成任务。如果有来自你先前生成内容的反馈，你应当参考这些反馈来优化你的解决方案。

            重要：你的回复必须是**一行**有效的 JSON，**不能包含换行符**，除非是通过 \\n 显式转义的。
            以下是你必须遵循的格式，包括所有引号和大括号：

            {"thoughts":"简要描述你的思路","response":"public class Example {\\n    // 代码写在这里\\n}"}

            response 字段的规则如下：
            1. 所有换行符必须使用 \\n
            2. 所有引号必须使用 \\"
            3. 所有反斜杠必须写成双反斜杠：\\\\
            4. **禁止**实际换行或格式化 - 所有内容必须在同一行
            5. 禁止使用制表符或特殊字符
            6. Java 代码必须是完整的，并正确进行转义

            格式示例（请严格模仿）：
            {"thoughts":"实现计数器","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

            请**严格**遵循该格式 - 你的输出必须是**一行有效 JSON 字符串**。
            """;

    /***
     * 评估部分提示词
     */
    public static final String DEFAULT_EVALUATOR_PROMPT = """
            请对以下代码实现进行评估，考虑以下方面：正确性、时间复杂度和最佳实践。
            请确保代码包含适当的 Javadoc 文档。
            请使用**完全相同格式的 JSON 字符串，且仅占用一行**进行回复：

            {"evaluation":"PASS、NEEDS_IMPROVEMENT 或 FAIL 之一", "feedback":"在此填写你的反馈"}

            evaluation 字段的取值只能是以下三种之一："PASS"、"NEEDS_IMPROVEMENT" 或 "FAIL"
            仅当所有评估标准都完全达标、无需改进时，才使用 "PASS"
            """;


    public static record Generation(String thoughts, String response) {

    }


    /***
     * 评估实体
     * @param evaluation
     * @param feedback
     */
    public static record EvaluationResponse(Evaluation evaluation, String feedback) {
        public enum Evaluation {
            PASS, NEEDS_IMPROVEMENT, FAIL
        }
    }

    public static record RefinedResponse(String solution, List<Generation> chainOfThought) {

    }

    private final ChatClient chatClient;

    private final String generatorPrompt;

    private final String evaluatorPrompt;

    public EvaluatorOptimizer(ChatClient chatClient) {
        this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
    }

    public EvaluatorOptimizer(ChatClient chatClient, String generatorPrompt, String evaluatorPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
        Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

        this.chatClient = chatClient;
        this.generatorPrompt = generatorPrompt;
        this.evaluatorPrompt = evaluatorPrompt;
    }

    public RefinedResponse loop(String task) {
        List<String> memory = new ArrayList<>();
        List<Generation> chainOfThought = new ArrayList<>();

        return loop(task, "", memory, chainOfThought);
    }

    public RefinedResponse loop(String task, String context, List<String> memory, List<Generation> chainOfThought) {

        // 1.生成部分
        Generation generation = generate(task, context);
        memory.add(generation.response());
        chainOfThought.add(generation);

        // 2.评估部分
        EvaluationResponse evaluationResponse = evaluate(generation.response(), task);


         //  3.通过则结束递归
        if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
            return new RefinedResponse(generation.response(), chainOfThought);
        }

        StringBuilder newContext = new StringBuilder();
        newContext.append("Previous attempts:");
        for (String m : memory) {
            newContext.append("\n- ").append(m);
        }
        newContext.append("\\nFeedback:").append(evaluationResponse.feedback());

        // 3.递归调用
        return loop(task, newContext.toString(), memory, chainOfThought);
    }


    /***
     * 生成
     * @param task
     * @param context
     * @return
     */
    private Generation generate(String task, String context) {
        Generation generationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\\n{context}\\nTask: {task}")
                        .param("prompt", this.generatorPrompt)
                        .param("context", context)
                        .param("task", task))
                .call()
                .entity(Generation.class);
        System.out.println(String.format("\n=== 生成输出 ===\n意图: %s\n\n返回结果:\n %s\n", generationResponse.thoughts(), generationResponse.response()));
        return generationResponse;
    }


    /***
     * 评估
     * @param content
     * @param task
     * @return
     */
    private EvaluationResponse evaluate(String content, String task) {

        EvaluationResponse evaluationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                        .param("prompt", this.evaluatorPrompt)
                        .param("task", task)
                        .param("content", content))
                .call()
                .entity(EvaluationResponse.class);

        System.out.println(String.format("\n=== 评估输出:===\n评估: %s\n\n反馈: %s\n", evaluationResponse.evaluation(), evaluationResponse.feedback()));

        return evaluationResponse;
    }


}
