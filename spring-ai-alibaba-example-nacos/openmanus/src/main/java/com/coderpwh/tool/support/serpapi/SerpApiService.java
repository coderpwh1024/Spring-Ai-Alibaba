package com.coderpwh.tool.support.serpapi;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static com.coderpwh.tool.support.serpapi.SerpApiProperties.SERP_API_URL;
import static com.coderpwh.tool.support.serpapi.SerpApiProperties.USER_AGENT_VALUE;

/**
 * @author coderpwh
 */
public class SerpApiService {

    private static final Logger logger = LoggerFactory.getLogger(SerpApiService.class);

    private final WebClient webClient;

    private final String apikey;

    private final String engine;

    public SerpApiService(SerpApiProperties properties) {
        this.apikey = properties.getApikey();
        this.engine = properties.getEngine();
        this.webClient = WebClient.builder()
                .baseUrl(SERP_API_URL)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    /**
     * 使用serpai API 搜索数据
     * @param request the function argument
     * @return responseMono
     */
    public Map<String, Object> apply(Request request) {
        if (request == null || !StringUtils.hasText(request.query)) {
            return null;
        }
        try {
            Mono<String> responseMono = webClient.method(HttpMethod.GET)
                    .uri(uriBuilder -> uriBuilder.queryParam("api_key", apikey)
                            .queryParam("engine", engine)
                            .queryParam("q", request.query)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class);
            String response = responseMono.block();
            assert response != null;
            logger.info("serpapi search: {},result:{}", request.query, response);
            return parseJson(response);
        }
        catch (Exception e) {
            logger.error("failed to invoke serpapi search, caused by:{}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> parseJson(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, Map.class);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("serpapi search request")
    public record Request(@JsonProperty(required = true,
            value = "query") @JsonPropertyDescription("The query " + "keyword e.g. Alibaba") String query) {
    }

    @JsonClassDescription("serpapi search response")
    public record Response(List<SearchResult> results) {
    }

    public record SearchResult(String title, String text) {
    }
}
