package com.coderpwh;

import com.coderpwh.work.RoutingWorkflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

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
        return args -> {
            Map<String, String> supportRoutes = Map.of(
                    "billing",
                    """
                            你是一名账单支持专员，请遵循以下指南：
                            1. 始终以“账单支持回复：”开头
                            2. 首先确认用户提出的具体账单问题
                            3. 清晰说明费用来源或差异原因
                            4. 列出明确的后续处理步骤及时间表
                            5. 如有必要，请说明可选的付款方式
                                            
                            回应应专业且友好。
                                            
                            输入：
                            """,

                    "technical",
                    """
                            你是一名技术支持工程师，请遵循以下指南：
                            1. 始终以“技术支持回复：”开头
                            2. 明确列出解决问题的具体步骤
                            3. 如有必要，说明系统要求
                            4. 提供常见问题的替代解决方案
                            5. 如需升级处理，请说明相关流程
                                            
                            使用清晰、有编号的步骤和技术细节。
                                            
                            输入：
                            """,

                    "account",
                    """
                            你是一名账户安全专员，请遵循以下指南：
                            1. 始终以“账户支持回复：”开头
                            2. 优先处理账户安全和身份验证
                            3. 提供账户找回或更改的清晰步骤
                            4. 包含安全提示和警示信息
                            5. 明确说明处理时间和用户期望
                                            
                            语气严肃，注重安全。
                                            
                            输入：
                            """,

                    "product",
                    """
                            你是一名产品专员，请遵循以下指南：
                            1. 始终以“产品支持回复：”开头
                            2. 注重功能讲解和使用建议
                            3. 提供具体的使用示例
                            4. 链接相关文档章节
                            5. 推荐其他可能有帮助的功能
                                            
                            保持教学式语气，积极鼓励用户。
                                            
                            输入：
                            """
            );

            List<String> tickets = List.of(
                    """
                            主题：无法登录账户
                            内容：你好，我这一个小时一直在尝试登录，但总是提示“密码无效”。
                            我很确定我输入的是正确密码。你能帮我恢复访问权限吗？这很紧急，我需要在今天提交报告。
                            - John
                            """,
                    """
                            主题：信用卡上有一笔异常扣费
                            内容：您好，我刚刚注意到我的信用卡上有一笔.99元的扣费，但我记得我订阅的是.99元的套餐。
                            请问这笔费用是怎么来的？如果有误的话能否帮我处理一下？
                            谢谢，
                            Sarah
                            """,
                    """
                            主题：如何导出数据？
                            内容：我需要把我的项目数据导出到 Excel。我查阅了文档，但没找到如何批量导出的方法。
                            请问可以做到吗？如果可以，能不能指导我具体步骤？
                            此致
                            Mike
                            """
            );

            var routerWorkflow = new RoutingWorkflow(chatClientBuilder.build());

            int i = 1;

            for (String ticket : tickets) {
                System.out.println("\nTicket " + i++);
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.println();
                System.out.println(ticket);
                System.out.println("--------------------------------------------------------------------------------------------");
                System.out.println("");
                System.out.println("路由信息:");
                System.out.println(routerWorkflow.route(ticket, supportRoutes));
                System.out.println("--------------------------------------------------------------------------------------------");
            }
        };
    }


}
