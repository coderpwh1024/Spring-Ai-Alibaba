package com.coderpwh.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author coderpwh
 */
public class DispatcherNode implements NodeAction {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherNode.class);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        logger.info("dispatcher node is running");

        Map<String, Object> updated = new HashMap<>();
        String expandStatus = state.value("expand_status", "");

        if (expandStatus.isEmpty()) {
            updated.put("expand_status", "assigned");
            logger.info("Set expand_status to assigned");
        } else {
            logger.info("expand_status already set to: {}", expandStatus);
        }

        String translateStatus = state.value("translate_status", "");
        if (translateStatus.isEmpty()) {
           updated.put("translate_status","assigned");
            logger.info("Set translate_status to assigned");
        } else {
            logger.info("translate_status already set to: {}", translateStatus);
        }

        return updated;
    }


}
