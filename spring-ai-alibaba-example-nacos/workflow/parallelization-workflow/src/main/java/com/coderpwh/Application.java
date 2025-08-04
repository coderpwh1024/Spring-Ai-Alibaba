package com.coderpwh;

import com.coderpwh.work.ParallelizationlWorkflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coderpwh
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {

        String prompt = """
                请分析新能源汽车在中国市场的发展前景。
                """;

        String strOne = """
                # 从政策支持角度分析
                请从中国政府对新能源汽车的政策支持角度进行分析，包括补贴政策、碳排放政策、新能源车牌照政策等，并评估这些政策对行业的推动作用。
                """;

        String strTwo = """
                # 从市场需求角度分析
                请从消费者需求和市场趋势的角度分析新能源汽车的发展前景，包括消费者关注的核心因素（如续航、价格、充电便利性）以及市场接受度变化。
                """;

        String strThree = """
                # 从技术发展角度分析
                请从技术创新的角度分析新能源汽车的发展，包括电池技术、智能驾驶、整车平台等关键技术的进展，以及它们对市场前景的影响。
                 """;

        List<String> list = new ArrayList<>();
        list.add(strOne);
        list.add(strTwo);
        list.add(strThree);

        return args -> {
            List<String> parallelResponse = new ParallelizationlWorkflow(chatClientBuilder.build())
                    .parallel(prompt, list, 4);
            System.out.println(parallelResponse);
        };

    }
}
