package com.example.rag.service;

import com.example.rag.model.Document;
import com.example.rag.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);

    @Value("${rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${rag.chunk-overlap:200}")
    private int chunkOverlap;

    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern PARAGRAPH_BOUNDARY = Pattern.compile("\\n\\s*\\n");

    public List<DocumentChunk> chunkDocument(Document document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String content = document.getContent();
        
        if (content == null || content.trim().isEmpty()) {
            logger.warn("Document {} has empty content", document.getId());
            return chunks;
        }

        List<String> textChunks = createSemanticChunks(content);
        
        for (int i = 0; i < textChunks.size(); i++) {
            String chunkContent = textChunks.get(i);
            String chunkId = document.getId() + "_chunk_" + i;
            
            DocumentChunk chunk = new DocumentChunk(chunkId, document.getId(), chunkContent, i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_title", document.getTitle());
            metadata.put("document_source", document.getSource());
            metadata.put("chunk_length", chunkContent.length());
            metadata.put("total_chunks", textChunks.size());
            
            chunk.setMetadata(metadata);
            chunks.add(chunk);
        }
        
        logger.info("Created {} chunks for document {}", chunks.size(), document.getId());
        return chunks;
    }

    private List<String> createSemanticChunks(String text) {
        List<String> chunks = new ArrayList<>();
        
        String[] paragraphs = PARAGRAPH_BOUNDARY.split(text);
        
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                continue;
            }
            
            if (currentChunk.length() + trimmedParagraph.length() <= chunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmedParagraph);
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    
                    String overlap = extractOverlap(currentChunk.toString());
                    currentChunk = new StringBuilder(overlap);
                    
                    if (currentChunk.length() > 0) {
                        currentChunk.append("\n\n");
                    }
                }
                
                if (trimmedParagraph.length() > chunkSize) {
                    List<String> sentenceChunks = chunkBySentences(trimmedParagraph);
                    chunks.addAll(sentenceChunks);
                } else {
                    currentChunk.append(trimmedParagraph);
                }
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }

    private List<String> chunkBySentences(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = SENTENCE_BOUNDARY.split(text);
        
        StringBuilder currentChunk = new StringBuilder();
        
        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (trimmedSentence.isEmpty()) {
                continue;
            }
            
            if (currentChunk.length() + trimmedSentence.length() <= chunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(trimmedSentence);
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    
                    String overlap = extractOverlap(currentChunk.toString());
                    currentChunk = new StringBuilder(overlap);
                    
                    if (currentChunk.length() > 0) {
                        currentChunk.append(" ");
                    }
                }
                
                if (trimmedSentence.length() > chunkSize) {
                    chunks.addAll(chunkByFixedSize(trimmedSentence));
                } else {
                    currentChunk.append(trimmedSentence);
                }
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }

    private List<String> chunkByFixedSize(String text) {
        List<String> chunks = new ArrayList<>();
        
        for (int i = 0; i < text.length(); i += chunkSize - chunkOverlap) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
            
            if (end == text.length()) {
                break;
            }
        }
        
        return chunks;
    }

    private String extractOverlap(String text) {
        if (text.length() <= chunkOverlap) {
            return text;
        }
        
        String overlap = text.substring(text.length() - chunkOverlap);
        
        int sentenceStart = overlap.indexOf(". ");
        if (sentenceStart != -1) {
            return overlap.substring(sentenceStart + 2);
        }
        
        return overlap;
    }

    public String extractTitle(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Untitled Document";
        }
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() <= 200) {
                return trimmed;
            }
        }
        
        String firstSentence = content.trim();
        int endIndex = Math.min(firstSentence.length(), 100);
        int sentenceEnd = firstSentence.indexOf(".", Math.min(50, firstSentence.length()));
        
        if (sentenceEnd > 0) {
            endIndex = sentenceEnd;
        }
        
        return firstSentence.substring(0, endIndex).trim();
    }

    public Map<String, Object> extractMetadata(String content, String source) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("word_count", countWords(content));
        metadata.put("character_count", content.length());
        metadata.put("paragraph_count", countParagraphs(content));
        metadata.put("source_type", detectSourceType(source));
        
        return metadata;
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private int countParagraphs(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return PARAGRAPH_BOUNDARY.split(text).length;
    }

    private String detectSourceType(String source) {
        if (source == null) {
            return "unknown";
        }
        
        String lowerSource = source.toLowerCase();
        if (lowerSource.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerSource.endsWith(".txt")) {
            return "text";
        } else if (lowerSource.endsWith(".md")) {
            return "markdown";
        } else if (lowerSource.startsWith("http")) {
            return "web";
        } else {
            return "document";
        }
    }
}