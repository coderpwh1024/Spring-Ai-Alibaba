package com.coderpwh.io.modelcontextprotocol.client;


import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.coderpwh.io.modelcontextprotocol.client.transport.StreamableHttpClientTransport;
import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.spec.McpClientSession;
import io.modelcontextprotocol.spec.McpClientSession.NotificationHandler;
import io.modelcontextprotocol.spec.McpClientSession.RequestHandler;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ListPromptsResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.PaginatedRequest;
import io.modelcontextprotocol.spec.McpSchema.Root;
import io.modelcontextprotocol.spec.McpTransport;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;



public class McpAsyncClient {

    private static final Logger logger = LoggerFactory.getLogger(McpAsyncClient.class);


    private static TypeReference<Void> VOID_TYPE_REFERENCE = new TypeReference<>() {
    };

    protected final Sinks.One<McpSchema.InitializeResult> initializedSink = Sinks.one();


    private AtomicBoolean initialized = new AtomicBoolean(false);


    private final Duration initializationTimeout;


    private final McpClientSession mcpSession;

    private final ClientCapabilities clientCapabilities;

    private final McpSchema.Implementation clientInfo;

    private McpSchema.ServerCapabilities serverCapabilities;

    private McpSchema.Implementation serverInfo;

    private final ConcurrentHashMap<String, Root> roots;

    private Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler;


    private final McpTransport transport;


    private List<String> protocolVersions = List.of(McpSchema.LATEST_PROTOCOL_VERSION);


    public  McpAsyncClient(McpClientTransport transport, Duration requestTimeout, Duration initializationTimeout,
                           McpClientFeatures.Async features) {

        Assert.notNull(transport, "Transport must not be null");
        Assert.notNull(requestTimeout, "Request timeout must not be null");
        Assert.notNull(initializationTimeout, "Initialization timeout must not be null");

        this.clientInfo = features.clientInfo();
        this.clientCapabilities = features.clientCapabilities();
        this.transport = transport;
        this.roots = new ConcurrentHashMap<>(features.roots());
        this.initializationTimeout = initializationTimeout;

        // Request Handlers
        Map<String, RequestHandler<?>> requestHandlers = new HashMap<>();

        // Roots List Request Handler
        if (this.clientCapabilities.roots() != null) {
            requestHandlers.put(McpSchema.METHOD_ROOTS_LIST, rootsListRequestHandler());
        }

        // Sampling Handler
        if (this.clientCapabilities.sampling() != null) {
            if (features.samplingHandler() == null) {
                throw new McpError("Sampling handler must not be null when client capabilities include sampling");
            }
            this.samplingHandler = features.samplingHandler();
            requestHandlers.put(McpSchema.METHOD_SAMPLING_CREATE_MESSAGE, samplingCreateMessageHandler());
        }

        // Notification Handlers
        Map<String, NotificationHandler> notificationHandlers = new HashMap<>();

        // Tools Change Notification
        List<Function<List<McpSchema.Tool>, Mono<Void>>> toolsChangeConsumersFinal = new ArrayList<>();
        toolsChangeConsumersFinal
                .add((notification) -> Mono.fromRunnable(() -> logger.debug("Tools changed: {}", notification)));

        if (!Utils.isEmpty(features.toolsChangeConsumers())) {
            toolsChangeConsumersFinal.addAll(features.toolsChangeConsumers());
        }
        notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED,
                asyncToolsChangeNotificationHandler(toolsChangeConsumersFinal));

        // Resources Change Notification
        List<Function<List<McpSchema.Resource>, Mono<Void>>> resourcesChangeConsumersFinal = new ArrayList<>();
        resourcesChangeConsumersFinal
                .add((notification) -> Mono.fromRunnable(() -> logger.debug("Resources changed: {}", notification)));

        if (!Utils.isEmpty(features.resourcesChangeConsumers())) {
            resourcesChangeConsumersFinal.addAll(features.resourcesChangeConsumers());
        }

        notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED,
                asyncResourcesChangeNotificationHandler(resourcesChangeConsumersFinal));

        // Prompts Change Notification
        List<Function<List<McpSchema.Prompt>, Mono<Void>>> promptsChangeConsumersFinal = new ArrayList<>();
        promptsChangeConsumersFinal
                .add((notification) -> Mono.fromRunnable(() -> logger.debug("Prompts changed: {}", notification)));
        if (!Utils.isEmpty(features.promptsChangeConsumers())) {
            promptsChangeConsumersFinal.addAll(features.promptsChangeConsumers());
        }
        notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED,
                asyncPromptsChangeNotificationHandler(promptsChangeConsumersFinal));

        // Utility Logging Notification
        List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumersFinal = new ArrayList<>();
        loggingConsumersFinal.add((notification) -> Mono.fromRunnable(() -> logger.debug("Logging: {}", notification)));
        if (!Utils.isEmpty(features.loggingConsumers())) {
            loggingConsumersFinal.addAll(features.loggingConsumers());
        }
        notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_MESSAGE,
                asyncLoggingNotificationHandler(loggingConsumersFinal));

        this.mcpSession = new McpClientSession(requestTimeout, transport, requestHandlers, notificationHandlers);

    }
    /**
     * Get the server capabilities that define the supported features and functionality.
     * @return The server capabilities
     */
    public McpSchema.ServerCapabilities getServerCapabilities() {
        return this.serverCapabilities;
    }

    /**
     * Get the server implementation information.
     * @return The server implementation details
     */
    public McpSchema.Implementation getServerInfo() {
        return this.serverInfo;
    }

    /**
     * Check if the client-server connection is initialized.
     * @return true if the client-server connection is initialized
     */
    public boolean isInitialized() {
        return this.initialized.get();
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return this.clientCapabilities;
    }

    /**
     * Get the client implementation information.
     * @return The client implementation details
     */
    public McpSchema.Implementation getClientInfo() {
        return this.clientInfo;
    }

    /**
     * Closes the client connection immediately.
     */
    public void close() {
        this.mcpSession.close();
    }

    /**
     * Gracefully closes the client connection.
     * @return A Mono that completes when the connection is closed
     */
    public Mono<Void> closeGracefully() {
        return this.mcpSession.closeGracefully();
    }

    public Mono<McpSchema.InitializeResult> initialize() {
        String latestVersion = this.protocolVersions.get(this.protocolVersions.size() - 1);

        McpSchema.InitializeRequest initializeRequest = new McpSchema.InitializeRequest(// @formatter:off
                latestVersion,
                this.clientCapabilities,
                this.clientInfo); // @formatter:on

        Mono<McpSchema.InitializeResult> result = this.mcpSession.sendRequest(McpSchema.METHOD_INITIALIZE,
                initializeRequest, new TypeReference<McpSchema.InitializeResult>() {
                });

        return result.flatMap(initializeResult -> {

            this.serverCapabilities = initializeResult.capabilities();
            this.serverInfo = initializeResult.serverInfo();

            logger.info("Server response with Protocol: {}, Capabilities: {}, Info: {} and Instructions {}",
                    initializeResult.protocolVersion(), initializeResult.capabilities(), initializeResult.serverInfo(),
                    initializeResult.instructions());

            if (!this.protocolVersions.contains(initializeResult.protocolVersion())) {
                return Mono.error(new McpError(
                        "Unsupported protocol version from the server: " + initializeResult.protocolVersion()));
            }
            if (this.transport instanceof StreamableHttpClientTransport) {
                this.initialized.set(true);
                this.initializedSink.tryEmitValue(initializeResult);
                return Mono.just(initializeResult);
            }
            else {
                return this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_INITIALIZED, null)
                        .doOnSuccess(v -> {
                            this.initialized.set(true);
                            this.initializedSink.tryEmitValue(initializeResult);
                        })
                        .thenReturn(initializeResult);
            }
        });
    }

    private <T> Mono<T> withInitializationCheck(String actionName,
                                                Function<McpSchema.InitializeResult, Mono<T>> operation) {
        return this.initializedSink.asMono()
                .timeout(this.initializationTimeout)
                .onErrorResume(TimeoutException.class,
                        ex -> Mono.error(new McpError("Client must be initialized before " + actionName)))
                .flatMap(operation);
    }

    public Mono<Object> ping() {
        return this.withInitializationCheck("pinging the server", initializedResult -> this.mcpSession
                .sendRequest(McpSchema.METHOD_PING, null, new TypeReference<Object>() {
                }));
    }

    public Mono<Void> addRoot(Root root) {

        if (root == null) {
            return Mono.error(new McpError("Root must not be null"));
        }

        if (this.clientCapabilities.roots() == null) {
            return Mono.error(new McpError("Client must be configured with roots capabilities"));
        }

        if (this.roots.containsKey(root.uri())) {
            return Mono.error(new McpError("Root with uri '" + root.uri() + "' already exists"));
        }

        this.roots.put(root.uri(), root);

        logger.debug("Added root: {}", root);

        if (this.clientCapabilities.roots().listChanged()) {
            if (this.isInitialized()) {
                return this.rootsListChangedNotification();
            }
            else {
                logger.warn("Client is not initialized, ignore sending a roots list changed notification");
            }
        }
        return Mono.empty();
    }

    public Mono<Void> removeRoot(String rootUri) {

        if (rootUri == null) {
            return Mono.error(new McpError("Root uri must not be null"));
        }

        if (this.clientCapabilities.roots() == null) {
            return Mono.error(new McpError("Client must be configured with roots capabilities"));
        }

        Root removed = this.roots.remove(rootUri);

        if (removed != null) {
            logger.debug("Removed Root: {}", rootUri);
            if (this.clientCapabilities.roots().listChanged()) {
                if (this.isInitialized()) {
                    return this.rootsListChangedNotification();
                }
                else {
                    logger.warn("Client is not initialized, ignore sending a roots list changed notification");
                }

            }
            return Mono.empty();
        }
        return Mono.error(new McpError("Root with uri '" + rootUri + "' not found"));
    }

    public Mono<Void> rootsListChangedNotification() {
        return this.withInitializationCheck("sending roots list changed notification",
                initResult -> this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED));
    }

    private RequestHandler<McpSchema.ListRootsResult> rootsListRequestHandler() {
        return params -> {
            @SuppressWarnings("unused")
            PaginatedRequest request = transport.unmarshalFrom(params, new TypeReference<PaginatedRequest>() {
            });

            List<Root> roots = this.roots.values().stream().toList();

            return Mono.just(new McpSchema.ListRootsResult(roots));
        };
    }

    private RequestHandler<CreateMessageResult> samplingCreateMessageHandler() {
        return params -> {
            CreateMessageRequest request = transport.unmarshalFrom(params, new TypeReference<CreateMessageRequest>() {
            });

            return this.samplingHandler.apply(request);
        };
    }

    private static final TypeReference<McpSchema.CallToolResult> CALL_TOOL_RESULT_TYPE_REF = new TypeReference<>() {};

    private static final TypeReference<McpSchema.ListToolsResult> LIST_TOOLS_RESULT_TYPE_REF = new TypeReference<>() {};

    public Mono<McpSchema.CallToolResult> callTool(McpSchema.CallToolRequest callToolRequest) {
        return this.withInitializationCheck("calling tools", initializedResult -> {
            if (this.serverCapabilities.tools() == null) {
                return Mono.error(new McpError("Server does not provide tools capability"));
            }
            return this.mcpSession.sendRequest(McpSchema.METHOD_TOOLS_CALL, callToolRequest, CALL_TOOL_RESULT_TYPE_REF);
        });
    }

    public Mono<McpSchema.ListToolsResult> listTools() {
        return this.listTools(null);
    }

    public Mono<McpSchema.ListToolsResult> listTools(String cursor) {
        return this.withInitializationCheck("listing tools", initializedResult -> {
            if (this.serverCapabilities.tools() == null) {
                return Mono.error(new McpError("Server does not provide tools capability"));
            }
            return this.mcpSession.sendRequest(McpSchema.METHOD_TOOLS_LIST, new PaginatedRequest(cursor),
                    LIST_TOOLS_RESULT_TYPE_REF);
        });
    }


    private NotificationHandler asyncToolsChangeNotificationHandler(
            List<Function<List<McpSchema.Tool>, Mono<Void>>> toolsChangeConsumers) {
        // TODO: params are not used yet
        return params -> this.listTools()
                .flatMap(listToolsResult -> Flux.fromIterable(toolsChangeConsumers)
                        .flatMap(consumer -> consumer.apply(listToolsResult.tools()))
                        .onErrorResume(error -> {
                            logger.error("Error handling tools list change notification", error);
                            return Mono.empty();
                        })
                        .then());
    }


    private static final TypeReference<McpSchema.ListResourcesResult> LIST_RESOURCES_RESULT_TYPE_REF = new TypeReference<>() {};

    private static final TypeReference<McpSchema.ReadResourceResult> READ_RESOURCE_RESULT_TYPE_REF = new TypeReference<>() {};

    private static final TypeReference<McpSchema.ListResourceTemplatesResult> LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF = new TypeReference<>() {};


    public Mono<McpSchema.ListResourcesResult> listResources() {
        return this.listResources(null);
    }

    public Mono<McpSchema.ListResourcesResult> listResources(String cursor) {
        return this.withInitializationCheck("listing resources", initializedResult -> {
            if (this.serverCapabilities.resources() == null) {
                return Mono.error(new McpError("Server does not provide the resources capability"));
            }
            return this.mcpSession.sendRequest(McpSchema.METHOD_RESOURCES_LIST, new PaginatedRequest(cursor),
                    LIST_RESOURCES_RESULT_TYPE_REF);
        });
    }

    public Mono<McpSchema.ReadResourceResult> readResource(McpSchema.Resource resource) {
        return this.readResource(new McpSchema.ReadResourceRequest(resource.uri()));
    }

    public Mono<McpSchema.ReadResourceResult> readResource(McpSchema.ReadResourceRequest readResourceRequest) {
        return this.withInitializationCheck("reading resources", initializedResult -> {
            if (this.serverCapabilities.resources() == null) {
                return Mono.error(new McpError("Server does not provide the resources capability"));
            }
            return this.mcpSession.sendRequest(McpSchema.METHOD_RESOURCES_READ, readResourceRequest,
                    READ_RESOURCE_RESULT_TYPE_REF);
        });
    }

    public Mono<McpSchema.ListResourceTemplatesResult> listResourceTemplates() {
        return this.listResourceTemplates(null);
    }

    public Mono<McpSchema.ListResourceTemplatesResult> listResourceTemplates(String cursor) {
        return this.withInitializationCheck("listing resource templates", initializedResult -> {
            if (this.serverCapabilities.resources() == null) {
                return Mono.error(new McpError("Server does not provide the resources capability"));
            }
            return this.mcpSession.sendRequest(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, new PaginatedRequest(cursor),
                    LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF);
        });
    }

    public Mono<Void> subscribeResource(McpSchema.SubscribeRequest subscribeRequest) {
        return this.withInitializationCheck("subscribing to resources", initializedResult -> this.mcpSession
                .sendRequest(McpSchema.METHOD_RESOURCES_SUBSCRIBE, subscribeRequest, VOID_TYPE_REFERENCE));
    }

    public Mono<Void> unsubscribeResource(McpSchema.UnsubscribeRequest unsubscribeRequest) {
        return this.withInitializationCheck("unsubscribing from resources", initializedResult -> this.mcpSession
                .sendRequest(McpSchema.METHOD_RESOURCES_UNSUBSCRIBE, unsubscribeRequest, VOID_TYPE_REFERENCE));
    }

    private NotificationHandler asyncResourcesChangeNotificationHandler(
            List<Function<List<McpSchema.Resource>, Mono<Void>>> resourcesChangeConsumers) {
        return params -> listResources().flatMap(listResourcesResult -> Flux.fromIterable(resourcesChangeConsumers)
                .flatMap(consumer -> consumer.apply(listResourcesResult.resources()))
                .onErrorResume(error -> {
                    logger.error("Error handling resources list change notification", error);
                    return Mono.empty();
                })
                .then());
    }

    private static final TypeReference<ListPromptsResult> LIST_PROMPTS_RESULT_TYPE_REF = new TypeReference<>() {};

    private static final TypeReference<GetPromptResult> GET_PROMPT_RESULT_TYPE_REF = new TypeReference<>() {};

    public Mono<ListPromptsResult> listPrompts() {
        return this.listPrompts(null);
    }

    public Mono<ListPromptsResult> listPrompts(String cursor) {
        return this.withInitializationCheck("listing prompts", initializedResult -> this.mcpSession
                .sendRequest(McpSchema.METHOD_PROMPT_LIST, new PaginatedRequest(cursor), LIST_PROMPTS_RESULT_TYPE_REF));
    }

    public Mono<GetPromptResult> getPrompt(GetPromptRequest getPromptRequest) {
        return this.withInitializationCheck("getting prompts", initializedResult -> this.mcpSession
                .sendRequest(McpSchema.METHOD_PROMPT_GET, getPromptRequest, GET_PROMPT_RESULT_TYPE_REF));
    }

    private NotificationHandler asyncPromptsChangeNotificationHandler(
            List<Function<List<McpSchema.Prompt>, Mono<Void>>> promptsChangeConsumers) {
        return params -> listPrompts().flatMap(listPromptsResult -> Flux.fromIterable(promptsChangeConsumers)
                .flatMap(consumer -> consumer.apply(listPromptsResult.prompts()))
                .onErrorResume(error -> {
                    logger.error("Error handling prompts list change notification", error);
                    return Mono.empty();
                })
                .then());
    }

    private NotificationHandler asyncLoggingNotificationHandler(
            List<Function<LoggingMessageNotification, Mono<Void>>> loggingConsumers) {

        return params -> {
            LoggingMessageNotification loggingMessageNotification = transport.unmarshalFrom(params,
                    new TypeReference<LoggingMessageNotification>() {
                    });

            return Flux.fromIterable(loggingConsumers)
                    .flatMap(consumer -> consumer.apply(loggingMessageNotification))
                    .then();
        };
    }

    public Mono<Void> setLoggingLevel(LoggingLevel loggingLevel) {
        if (loggingLevel == null) {
            return Mono.error(new McpError("Logging level must not be null"));
        }

        return this.withInitializationCheck("setting logging level", initializedResult -> {
            var params = new McpSchema.SetLevelRequest(loggingLevel);
            return this.mcpSession.sendRequest(McpSchema.METHOD_LOGGING_SET_LEVEL, params, new TypeReference<Object>() {
            }).then();
        });
    }

    void setProtocolVersions(List<String> protocolVersions) {
        this.protocolVersions = protocolVersions;
    }







}
