package com.coderpwh.work;

import org.springframework.ai.chat.client.ChatClient;

/**
 * @author coderpwh
 */
public class ChainWorkflow {


    private static final String[] DEFAULT_SYSTEM_PROMPTS = {

            // Step 1
            """
					Extract only the numerical values and their associated metrics from the text.
					Format each as'value: metric' on a new line.
					Example format:
					92: customer satisfaction
					45%: revenue growth""",
            // Step 2
            """
					Convert all numerical values to percentages where possible.
					If not a percentage or points, convert to decimal (e.g., 92 points -> 92%).
					Keep one number per line.
					Example format:
					92%: customer satisfaction
					45%: revenue growth""",
            // Step 3
            """
					Sort all lines in descending order by numerical value.
					Keep the format 'value: metric' on each line.
					Example:
					92%: customer satisfaction
					87%: employee satisfaction""",
            // Step 4
            """
					Format the sorted data as a markdown table with columns:
					| Metric | Value |
					|:--|--:|
					| Customer Satisfaction | 92% | """
    };


    private final ChatClient chatClient;

    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }


    public String chain(String userInput) {
        int step = 0;

        String response = userInput;

        for (String prompt : systemPrompts) {
            String input = String.format("{%s}\n {%s}", prompt, response);

            response = chatClient.prompt(input).call().content();

            System.out.println(String.format("\\nSTEP %s:\\n %s", step++, response));
        }

        return response;
    }


}
