package com.example.rag;

import com.example.rag.service.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author coderpwh
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class AgenticRagSystemApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AgenticRagSystemApplication.class);

    @Autowired
    private ElasticsearchService elasticsearchService;

    public static void main(String[] args) {
        SpringApplication.run(AgenticRagSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Agentic RAG System...");
        
        try {
            elasticsearchService.initializeIndices();
            logger.info("Elasticsearch indices initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Elasticsearch indices: ", e);
        }
        
        logger.info("Agentic RAG System started successfully!");
        logger.info("Available endpoints:");
        logger.info("  POST /api/documents/upload - Upload and process documents");
        logger.info("  GET  /api/documents/search - Search documents");
        logger.info("  POST /api/query - Query with RAG or Agentic workflow");
        logger.info("  GET  /api/conversations - Manage conversations");
    }
}