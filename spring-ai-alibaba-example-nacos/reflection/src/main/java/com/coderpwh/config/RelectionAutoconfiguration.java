package com.coderpwh.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.ReflectAgent;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author coderpwh
 */
@Configuration
public class RelectionAutoconfiguration {

    public static class AssistantGraphNode implements NodeAction {

        private final LlmNode llmNode;

        private SystemPromptTemplate systemPromptTemplate;


        private final String NODE_ID = "call_model";

        private static final String CLASSIFIER_PROMPT_TEMPLATE = """
                	You are an essay assistant tasked with writing excellent 5-paragraph essays.
                    Generate the best essay possible for the user's request.
                    If the user provides critique, respond with a revised version of your previous attempts.
                    Only return the main content I need, without adding any other interactive language.
                    Please answer in Chinese:
                """;

        public AssistantGraphNode(ChatClient chatClient) {
            this.systemPromptTemplate = new SystemPromptTemplate(CLASSIFIER_PROMPT_TEMPLATE);
            this.llmNode = LlmNode.builder()
                    .systemPromptTemplate(systemPromptTemplate.render())
                    .chatClient(chatClient)
                    .messagesKey("messages")
                    .build();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ChatClient chatClient;

            public Builder chatClient(ChatClient chatClient) {
                this.chatClient = chatClient;
                return this;
            }

            public AssistantGraphNode build() {
                if (chatClient == null) {
                    throw new IllegalArgumentException("ChatClient must be provided");
                }
                return new AssistantGraphNode(chatClient);
            }
        }


        @Override
        public Map<String, Object> apply(OverAllState overAllState) throws Exception {
            List<Message> messages = (List<Message>) overAllState.value(ReflectAgent.MESSAGES).get();

            OverAllStateFactory stateFactory = () -> {
                OverAllState state = new OverAllState();
                state.registerKeyAndStrategy(ReflectAgent.MESSAGES, new AppendStrategy());
                return state;
            };

            StateGraph stateGraph = new StateGraph(stateFactory).addNode(this.NODE_ID, AsyncNodeAction.node_async(llmNode))
                    .addEdge(StateGraph.START, this.NODE_ID)
                    .addEdge(this.NODE_ID, StateGraph.END);

            OverAllState invokeState = stateGraph.compile().invoke(Map.of(ReflectAgent.MESSAGES, messages)).get();
            List<Message> resultMessages = (List<Message>) invokeState.value(ReflectAgent.MESSAGES).orElseThrow();

            return Map.of(ReflectAgent.MESSAGES, resultMessages);
        }
    }


    public static class JudgeGraphNode implements NodeAction {

        private final LlmNode llmNode;

        private final String NODE_ID = "judge_response";

        private SystemPromptTemplate systemPromptTemplate;

        private static final String CLASSIFIER_PROMPT_TEMPLATE = """
                	You are a teacher grading a student's essay submission. Provide detailed feedback and revision suggestions for the essay.

                	Your feedback should cover the following aspects:

                	- Length : Is the essay sufficiently developed? Does it meet the required length or need expansion/shortening?
                	- Depth : Are the ideas well-developed? Is there sufficient analysis, evidence, or explanation?
                	- Structure : Is the organization logical and clear? Are the introduction, transitions, and conclusion effective?
                	- Style and Tone : Is the writing style appropriate for the purpose and audience? Is the tone consistent and professional?
                	- Language Use : Are vocabulary, grammar, and sentence structure accurate and varied?
                	- Focus only on providing actionable suggestions for improvement. Do not include grades, scores, or overall summary evaluations.

                	Please respond in Chinese .
                """;

        public JudgeGraphNode(ChatClient chatClient) {
            this.systemPromptTemplate = new SystemPromptTemplate(CLASSIFIER_PROMPT_TEMPLATE);
            this.llmNode = LlmNode.builder()
                    .systemPromptTemplate(systemPromptTemplate.render())
                    .chatClient(chatClient)
                    .messagesKey(ReflectAgent.MESSAGES)
                    .build();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ChatClient chatClient;

            public JudgeGraphNode.Builder chatClient(ChatClient chatClient) {
                this.chatClient = chatClient;
                return this;
            }

            public JudgeGraphNode build() {
                if (chatClient == null) {
                    throw new IllegalArgumentException("ChatClient must be provided");
                }
                return new JudgeGraphNode(chatClient);
            }
        }


        @Override
        public Map<String, Object> apply(OverAllState allState) throws Exception {

            List<Message> messages = (List<Message>) allState.value(ReflectAgent.MESSAGES).get();

            OverAllStateFactory stateFactory = () -> {
                OverAllState state = new OverAllState();
                state.registerKeyAndStrategy(ReflectAgent.MESSAGES, new AppendStrategy());
                return state;
            };

            StateGraph stateGraph = new StateGraph(stateFactory).addNode(this.NODE_ID, AsyncNodeAction.node_async(llmNode))
                    .addEdge(StateGraph.START, this.NODE_ID)
                    .addEdge(this.NODE_ID, StateGraph.END);

            CompiledGraph compile = stateGraph.compile();

            OverAllState invokeState = compile.invoke(Map.of(ReflectAgent.MESSAGES, messages)).get();

            UnaryOperator<List<Message>> convertLastToUserMessage = messageList -> {

                int size = messageList.size();
                if (size == 0) {
                    return messageList;
                }
                Message last = messageList.get(size - 1);

                messageList.set(size - 1, new UserMessage(last.getText()));
                return messageList;
            };

            List<Message> reactMessages = (List<Message>) invokeState.value(ReflectAgent.MESSAGES).orElseThrow();
            convertLastToUserMessage.apply(reactMessages);

            return Map.of(ReflectAgent.MESSAGES, reactMessages);

        }

    }


}
