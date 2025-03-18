package com.coderpwh.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author coderpwh
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureConfig {

    private String endpoint;

    private String apiKey;

    private String modelName;

}
