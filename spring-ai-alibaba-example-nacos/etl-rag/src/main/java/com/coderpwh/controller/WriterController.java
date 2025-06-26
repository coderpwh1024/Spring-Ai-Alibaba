package com.coderpwh.controller;

import com.coderpwh.model.Constant;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author coderpwh
 */
@RequestMapping("/writer")
@RestController
public class WriterController {

    private static final Logger logger = Logger.getLogger(WriterController.class.getName());

    private final List<Document> documents;


    private final SimpleVectorStore simpleVectorStore;


    public WriterController(EmbeddingModel embeddingModel) {
        logger.info("start write file");
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource);
        this.documents = pagePdfDocumentReader.read();
        this.simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }


    @GetMapping("/file")
    public void writeFile(){
        String fileName="output.txt";
        FileDocumentWriter fileDocumentWriter = new FileDocumentWriter(fileName);
        fileDocumentWriter.write(documents);
    }

    @GetMapping("/vector")
    public void writeVector(){
        logger.info("写入向量");
        simpleVectorStore.add(documents);
    }

    @GetMapping("/search")
    public List<Document> search() {
        logger.info("start search data");
        return simpleVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring")
                .topK(2)
                .build());
    }




}
