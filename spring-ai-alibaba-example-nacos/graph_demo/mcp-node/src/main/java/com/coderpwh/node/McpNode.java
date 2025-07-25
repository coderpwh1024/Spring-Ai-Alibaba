package com.coderpwh.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * @author coderpwh
 */

public class McpNode implements NodeAction {

    private static  final Logger logger = LoggerFactory.getLogger(McpNode.class);


    private  static  final  String NODE_NAME = "mcp-node";


//    private  final ChatClient chatClient;






    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        return null;
    }


}
