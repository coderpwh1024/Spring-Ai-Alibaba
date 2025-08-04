package com.example.rag.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.rag.model.Document;
import com.example.rag.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Value("${elasticsearch.index.documents}")
    private String documentsIndex;

    @Value("${elasticsearch.index.conversations}")
    private String conversationsIndex;

    public void initializeIndices() {
        try {
            createIndexIfNotExists(documentsIndex);
            createIndexIfNotExists(conversationsIndex);
        } catch (IOException e) {
            logger.error("Error initializing indices: ", e);
        }
    }

    private void createIndexIfNotExists(String indexName) throws IOException {
        ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
        boolean exists = elasticsearchClient.indices().exists(existsRequest).value();
        
        if (!exists) {
            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
                .index(indexName)
                .mappings(m -> m
                    .properties("content", p -> p.text(t -> t.analyzer("standard")))
                    .properties("title", p -> p.text(t -> t.analyzer("standard")))
                    .properties("embedding", p -> p.denseVector(d -> d.dims(1536)))
                    .properties("created_at", p -> p.date(d -> d))
                    .properties("updated_at", p -> p.date(d -> d))
                )
            );
            
            elasticsearchClient.indices().create(createIndexRequest);
            logger.info("Created index: {}", indexName);
        }
    }

    public String indexDocument(Document document) {
        try {
            IndexRequest<Document> request = IndexRequest.of(i -> i
                .index(documentsIndex)
                .id(document.getId())
                .document(document)
            );
            
            IndexResponse response = elasticsearchClient.index(request);
            logger.debug("Indexed document: {} with result: {}", document.getId(), response.result());
            return response.id();
        } catch (IOException e) {
            logger.error("Error indexing document: ", e);
            throw new RuntimeException("Failed to index document", e);
        }
    }

    public String indexDocumentChunk(DocumentChunk chunk) {
        try {
            IndexRequest<DocumentChunk> request = IndexRequest.of(i -> i
                .index(documentsIndex)
                .id(chunk.getChunkId())
                .document(chunk)
            );
            
            IndexResponse response = elasticsearchClient.index(request);
            logger.debug("Indexed chunk: {} with result: {}", chunk.getChunkId(), response.result());
            return response.id();
        } catch (IOException e) {
            logger.error("Error indexing document chunk: ", e);
            throw new RuntimeException("Failed to index document chunk", e);
        }
    }

    public List<Document> searchDocuments(String query, int size) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(documentsIndex)
                .query(q -> q
                    .multiMatch(m -> m
                        .query(query)
                        .fields("title^2", "content")
                    )
                )
                .size(size)
            );
            
            SearchResponse<Document> response = elasticsearchClient.search(searchRequest, Document.class);
            
            return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error searching documents: ", e);
            return new ArrayList<>();
        }
    }

    public List<DocumentChunk> searchSimilarChunks(List<Double> queryEmbedding, double threshold, int size) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(documentsIndex)
                .query(q -> q
                    .scriptScore(ss -> ss
                        .query(Query.of(mq -> mq.matchAll(ma -> ma)))
                        .script(sc -> sc
                            .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                            .params("query_vector", queryEmbedding.toArray())
                        )
                        .minScore((float) threshold)
                    )
                )
                .size(size)
            );
            
            SearchResponse<DocumentChunk> response = elasticsearchClient.search(searchRequest, DocumentChunk.class);
            
            return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error searching similar chunks: ", e);
            return new ArrayList<>();
        }
    }

    public Document getDocumentById(String id) {
        try {
            GetRequest getRequest = GetRequest.of(g -> g
                .index(documentsIndex)
                .id(id)
            );
            
            GetResponse<Document> response = elasticsearchClient.get(getRequest, Document.class);
            
            if (response.found()) {
                return response.source();
            }
            return null;
        } catch (IOException e) {
            logger.error("Error getting document by id: ", e);
            return null;
        }
    }

    public boolean deleteDocument(String id) {
        try {
            DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                .index(documentsIndex)
                .id(id)
            );
            
            DeleteResponse response = elasticsearchClient.delete(deleteRequest);
            logger.debug("Deleted document: {} with result: {}", id, response.result());
            return response.result().toString().equals("deleted");
        } catch (IOException e) {
            logger.error("Error deleting document: ", e);
            return false;
        }
    }
}