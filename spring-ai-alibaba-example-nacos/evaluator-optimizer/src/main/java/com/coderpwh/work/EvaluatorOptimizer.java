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


    public static final String DEFAULT_GENERATOR_PROMPT = """
            Your goal is to complete the task based on the input. If there are feedback
            from your previous generations, you should reflect on them to improve your solution.

            CRITICAL: Your response must be a SINGLE LINE of valid JSON with NO LINE BREAKS except those explicitly escaped with \\n.
            Here is the exact format to follow, including all quotes and braces:

            {"thoughts":"Brief description here","response":"public class Example {\\n    // Code here\\n}"}

            Rules for the response field:
            1. ALL line breaks must use \\n
            2. ALL quotes must use \\"
            3. ALL backslashes must be doubled: \\
            4. NO actual line breaks or formatting - everything on one line
            5. NO tabs or special characters
            6. Java code must be complete and properly escaped

            Example of properly formatted response:
            {"thoughts":"Implementing counter","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

            Follow this format EXACTLY - your response must be valid JSON on a single line.
            """;

    public static final String DEFAULT_EVALUATOR_PROMPT = """
            Evaluate this code implementation for correctness, time complexity, and best practices.
            Ensure the code have proper javadoc documentation.
            Respond with EXACTLY this JSON format on a single line:

            {"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"Your feedback here"}

            The evaluation field must be one of: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
            Use "PASS" only if all criteria are met with no improvements needed.
            """;

    public static record Generation(String thoughts, String response) {
    }


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

        Generation generation = generate(task, context);
        memory.add(generation.response());
        chainOfThought.add(generation);

        EvaluationResponse evaluationResponse = evaluate(generation.response(), task);

        if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
            return new RefinedResponse(generation.response(), chainOfThought);
        }

        StringBuilder newContext = new StringBuilder();
        newContext.append("Previous attempts:");
        for (String m : memory) {
            newContext.append("\n- ").append(m);
        }
        newContext.append("\\nFeedback:").append(evaluationResponse.feedback());

        return loop(task, newContext.toString(), memory, chainOfThought);
    }

    private Generation generate(String task, String context) {
        Generation generationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\\n{context}\\nTask: {task}")
                        .param("prompt", this.generatorPrompt)
                        .param("context", context)
                        .param("task", task)).call().entity(Generation.class);

        System.out.println(String.format("\n=== GENERATOR OUTPUT ===\nTHOUGHTS: %s\n\nRESPONSE:\n %s\n",
                generationResponse.thoughts(), generationResponse.response()));
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
                .call().entity(EvaluationResponse.class);

        System.out.println(String.format("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n",
                evaluationResponse.evaluation(), evaluationResponse.feedback()));

        return evaluationResponse;
    }


}
