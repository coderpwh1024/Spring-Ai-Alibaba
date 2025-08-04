package com.example.rag.service;

import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.FunctionDefinition;
import com.example.rag.model.AgenticTool;
import com.example.rag.model.AgenticWorkflowState;
import com.example.rag.model.DocumentChunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author coderpwh
 */
@Service
public class AgenticWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(AgenticWorkflowService.class);

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${agentic.max-iterations:5}")
    private int maxIterations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, AgenticTool> registeredTools = new HashMap<>();

    public AgenticWorkflowService() {
        initializeTools();
    }

    private void initializeTools() {
        AgenticTool searchTool = new AgenticTool(
            "search",
            "Search through the document knowledge base for relevant information",
            Map.of(
                "query", Map.of("type", "string", "description", "Search query"),
                "max_results", Map.of("type", "integer", "description", "Maximum number of results")
            ),
            true
        );

        AgenticTool calculateTool = new AgenticTool(
            "calculate",
            "Perform mathematical calculations",
            Map.of(
                "expression", Map.of("type", "string", "description", "Mathematical expression to evaluate")
            ),
            true
        );

        AgenticTool summarizeTool = new AgenticTool(
            "summarize",
            "Summarize provided text content",
            Map.of(
                "text", Map.of("type", "string", "description", "Text to summarize"),
                "max_length", Map.of("type", "integer", "description", "Maximum length of summary")
            ),
            true
        );

        registeredTools.put("search", searchTool);
        registeredTools.put("calculate", calculateTool);
        registeredTools.put("summarize", summarizeTool);
    }

    public CompletableFuture<String> executeAgenticWorkflow(String sessionId, String userQuery) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AgenticWorkflowState state = getOrCreateWorkflowState(sessionId, userQuery);
                
                while (!state.isCompleted() && !state.hasReachedMaxIterations()) {
                    state.incrementIteration();
                    
                    String prompt = buildAgenticPrompt(state);
                    List<ChatCompletionsFunctionToolDefinition> functions = buildFunctionDefinitions();
                    
                    CompletableFuture<String> responseFuture = azureOpenAIService.generateWithFunctions(prompt, functions);
                    String response = responseFuture.join();
                    
                    state.addToHistory("Iteration " + state.getIterationCount() + ": " + response);
                    
                    if (response.contains("Function call:")) {
                        handleToolCalls(state, response);
                    } else {
                        state.setCompleted(true);
                        state.setFinalResponse(response);
                    }
                    
                    saveWorkflowState(state);
                }
                
                if (!state.isCompleted()) {
                    state.setFinalResponse("I've reached the maximum number of iterations. Here's what I found: " + 
                        String.join("\n", state.getExecutionHistory()));
                    state.setCompleted(true);
                    saveWorkflowState(state);
                }
                
                return state.getFinalResponse();
                
            } catch (Exception e) {
                logger.error("Error executing agentic workflow: ", e);
                return "I apologize, but I encountered an error while processing your request. Please try again.";
            }
        });
    }

    private AgenticWorkflowState getOrCreateWorkflowState(String sessionId, String userQuery) {
        String key = "agentic_workflow:" + sessionId;
        AgenticWorkflowState state = (AgenticWorkflowState) redisTemplate.opsForValue().get(key);
        
        if (state == null) {
            state = new AgenticWorkflowState(sessionId, userQuery);
            state.setMaxIterations(maxIterations);
            state.setAvailableTools(new ArrayList<>(registeredTools.values()));
        }
        
        return state;
    }

    private void saveWorkflowState(AgenticWorkflowState state) {
        String key = "agentic_workflow:" + state.getSessionId();
        redisTemplate.opsForValue().set(key, state, 1, TimeUnit.HOURS);
    }

    private String buildAgenticPrompt(AgenticWorkflowState state) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an intelligent agent that can use tools to help answer questions. ");
        prompt.append("Your task is: ").append(state.getCurrentTask()).append("\n\n");
        
        prompt.append("Available tools:\n");
        for (AgenticTool tool : state.getAvailableTools()) {
            prompt.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
        }
        prompt.append("\n");
        
        if (!state.getExecutionHistory().isEmpty()) {
            prompt.append("Previous actions:\n");
            for (String entry : state.getExecutionHistory()) {
                prompt.append("- ").append(entry).append("\n");
            }
            prompt.append("\n");
        }
        
        if (!state.getContext().isEmpty()) {
            prompt.append("Context information:\n");
            state.getContext().forEach((key, value) -> 
                prompt.append("- ").append(key).append(": ").append(value).append("\n"));
            prompt.append("\n");
        }
        
        prompt.append("Based on the task and available information, decide whether you need to use a tool or if you can provide a final answer. ");
        prompt.append("If you need more information, use the appropriate tool. If you have enough information, provide a comprehensive final answer.");
        
        return prompt.toString();
    }

    private List<ChatCompletionsFunctionToolDefinition> buildFunctionDefinitions() {
        List<ChatCompletionsFunctionToolDefinition> functions = new ArrayList<>();
        
        for (AgenticTool tool : registeredTools.values()) {
            try {
                String parametersJson = objectMapper.writeValueAsString(Map.of(
                    "type", "object",
                    "properties", tool.getParameters(),
                    "required", tool.isRequired() ? Arrays.asList(tool.getParameters().keySet().toArray()) : Collections.emptyList()
                ));
                
                FunctionDefinition functionDef = new FunctionDefinition(tool.getName())
                    .setDescription(tool.getDescription())
                    .setParameters(parametersJson);
                
                functions.add(new ChatCompletionsFunctionToolDefinition(functionDef));
            } catch (JsonProcessingException e) {
                logger.error("Error creating function definition for tool {}: ", tool.getName(), e);
            }
        }
        
        return functions;
    }

    private void handleToolCalls(AgenticWorkflowState state, String response) {
        if (response.contains("search")) {
            handleSearchTool(state, response);
        } else if (response.contains("calculate")) {
            handleCalculateTool(state, response);
        } else if (response.contains("summarize")) {
            handleSummarizeTool(state, response);
        }
    }

    private void handleSearchTool(AgenticWorkflowState state, String response) {
        try {
            String query = extractArgumentFromResponse(response, "query");
            if (query != null) {
                CompletableFuture<List<DocumentChunk>> searchFuture = ragService.retrieveRelevantContext(query);
                List<DocumentChunk> results = searchFuture.join();
                
                StringBuilder searchResults = new StringBuilder("Search results for: " + query + "\n");
                for (int i = 0; i < Math.min(results.size(), 5); i++) {
                    DocumentChunk chunk = results.get(i);
                    searchResults.append("Result ").append(i + 1).append(": ")
                        .append(chunk.getContent().substring(0, Math.min(200, chunk.getContent().length())))
                        .append("...\n");
                }
                
                state.addContextData("search_results", searchResults.toString());
                state.addToHistory("Searched for: " + query + " - Found " + results.size() + " results");
            }
        } catch (Exception e) {
            logger.error("Error handling search tool: ", e);
            state.addToHistory("Search failed: " + e.getMessage());
        }
    }

    private void handleCalculateTool(AgenticWorkflowState state, String response) {
        try {
            String expression = extractArgumentFromResponse(response, "expression");
            if (expression != null) {
                double result = evaluateExpression(expression);
                state.addContextData("calculation_result", result);
                state.addToHistory("Calculated: " + expression + " = " + result);
            }
        } catch (Exception e) {
            logger.error("Error handling calculate tool: ", e);
            state.addToHistory("Calculation failed: " + e.getMessage());
        }
    }

    private void handleSummarizeTool(AgenticWorkflowState state, String response) {
        try {
            String text = extractArgumentFromResponse(response, "text");
            if (text != null) {
                String prompt = "Please provide a concise summary of the following text:\n\n" + text;
                CompletableFuture<String> summaryFuture = azureOpenAIService.generateCompletion(prompt, 0.3, 200);
                String summary = summaryFuture.join();
                
                state.addContextData("summary", summary);
                state.addToHistory("Summarized text - Result: " + summary);
            }
        } catch (Exception e) {
            logger.error("Error handling summarize tool: ", e);
            state.addToHistory("Summarization failed: " + e.getMessage());
        }
    }

    private String extractArgumentFromResponse(String response, String argumentName) {
        try {
            int start = response.indexOf(argumentName + "\":");
            if (start == -1){
                return null;
            }

            start = response.indexOf("\"", start + argumentName.length() + 2) + 1;
            int end = response.indexOf("\"", start);
            
            if (start > 0 && end > start) {
                return response.substring(start, end);
            }
        } catch (Exception e) {
            logger.error("Error extracting argument {}: ", argumentName, e);
        }
        return null;
    }

    private double evaluateExpression(String expression) {
        try {
            expression = expression.replaceAll("[^0-9+\\-*/().\\s]", "");
            
            if (expression.matches("^[0-9+\\-*/().\\s]+$")) {
                return evaluateSimpleExpression(expression);
            }
            
            throw new IllegalArgumentException("Invalid expression: " + expression);
        } catch (Exception e) {
            logger.warn("Could not evaluate expression: {}", expression);
            return 0.0;
        }
    }

    private double evaluateSimpleExpression(String expression) {
        expression = expression.replaceAll("\\s", "");
        
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+", 2);
            return evaluateSimpleExpression(parts[0]) + evaluateSimpleExpression(parts[1]);
        } else if (expression.contains("-") && expression.lastIndexOf("-") > 0) {
            int lastMinus = expression.lastIndexOf("-");
            return evaluateSimpleExpression(expression.substring(0, lastMinus)) - 
                   evaluateSimpleExpression(expression.substring(lastMinus + 1));
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*", 2);
            return evaluateSimpleExpression(parts[0]) * evaluateSimpleExpression(parts[1]);
        } else if (expression.contains("/")) {
            String[] parts = expression.split("/", 2);
            return evaluateSimpleExpression(parts[0]) / evaluateSimpleExpression(parts[1]);
        }
        
        return Double.parseDouble(expression);
    }
}