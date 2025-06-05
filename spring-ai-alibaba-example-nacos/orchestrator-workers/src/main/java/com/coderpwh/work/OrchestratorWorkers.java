package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author coderpwh
 */
public class OrchestratorWorkers {

    private final ChatClient chatClient;

    private final String orchestratorPrompt;

    private final String workerPrompt;

    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            Analyze this task and break it down into 2-3 distinct approaches:

            Task: {task}

            Return your response in this JSON format:
            \\{
            "analysis": "Explain your understanding of the task and which variations would be valuable.
                         Focus on how each approach serves different aspects of the task.",
            "tasks": [
            	\\{
            	"type": "formal",
            	"description": "Write a precise, technical version that emphasizes specifications"
            	\\},
            	\\{
            	"type": "conversational",
            	"description": "Write an engaging, friendly version that connects with readers"
            	\\}
            ]
            \\}
            """;

    public static final String DEFAULT_WORKER_PROMPT = """
            Generate content based on:
            Task: {original_task}
            Style: {task_type}
            Guidelines: {task_description}
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

        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt).param("task", taskDescription))
                .call().entity(OrchestratorResponse.class);

        System.out.println(String.format("\n=== ORCHESTRATOR OUTPUT ===\nANALYSIS: %s\n\nTASKS: %s\n",
                orchestratorResponse.analysis(), orchestratorResponse.tasks()));

        List<String> workerResponses = orchestratorResponse.tasks()
                .stream()
                .map(task -> this.chatClient.prompt()
                        .user(u -> u.text(this.workerPrompt)
                                .param("original_task", taskDescription)
                                .param("task_type", task.type())
                                .param("task_description", task.description())).call().content()).toList();

        System.out.println("\n=== WORKER OUTPUT ===\n" + workerResponses);

        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }


}
