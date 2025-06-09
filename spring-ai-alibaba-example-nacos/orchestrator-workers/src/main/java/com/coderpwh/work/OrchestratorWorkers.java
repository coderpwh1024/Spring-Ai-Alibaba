package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 协调器
 *
 * @author coderpwh
 */
public class OrchestratorWorkers {

    private final ChatClient chatClient;

    private final String orchestratorPrompt;

    private final String workerPrompt;

    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            分析这项任务，并将其分解为2-3种不同的方法：

            任务: {task}

            以JSON格式返回
            \\{
            "analysis": "解释你对任务的理解，以及哪些变化是有价值的。关注每种方法如何服务于任务的不同方面。",
            "tasks": [
            	\\{
            	"type": "formal",
            	"description": "写一个精确的技术版本，强调规格"
            	\\},
            	\\{
            	"type": "conversational",
            	"description": "写一个吸引人、友好的版本，与读者建立联系"
            	\\}
            ]
            \\}
            """;

    public static final String DEFAULT_WORKER_PROMPT = """
            生成的内容如下:
            任务: {original_task}
            风格: {task_type}
            指向: {task_description}
            """;

    public static record Task(String type, String description) {

    }

    public static record OrchestratorResponse(String analysis, List<Task> tasks) {

    }

    public static record FinalResponse(String analysis, List<String> workerResponses) {

    }

    public OrchestratorWorkers(ChatClient chatClient) {
        this(chatClient, DEFAULT_ORCHESTRATOR_PROMPT, DEFAULT_WORKER_PROMPT);
    }

    public OrchestratorWorkers(ChatClient chatClient, String orchestratorPrompt, String workerPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.notNull(orchestratorPrompt, "Orchestrator prompt must not be null");
        Assert.notNull(workerPrompt, "Worker prompt must not be null");

        this.chatClient = chatClient;
        this.orchestratorPrompt = orchestratorPrompt;
        this.workerPrompt = workerPrompt;
    }

    @SuppressWarnings("null")
    public FinalResponse process(String taskDescription) {
        Assert.hasText(taskDescription, "Task description must not be empty");

        // 协调器
        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt).param("task", taskDescription))
                .call().entity(OrchestratorResponse.class);

        System.out.println(String.format("\n=== ORCHESTRATOR OUTPUT ===\n分析: %s\n\n任务: %s\n",
                orchestratorResponse.analysis(), orchestratorResponse.tasks()));

        // work工作
        List<String> workerResponses = orchestratorResponse.tasks()
                .stream()
                .map(task -> this.chatClient.prompt()
                        .user(u -> u.text(this.workerPrompt)
                                .param("original_task", taskDescription)
                                .param("task_type", task.type())
                                .param("task_description", task.description()))
                        .call().content()).toList();

        System.out.println("\n=== WORKER OUTPUT ===\n" + workerResponses);

        // 最终返回
        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }


}
