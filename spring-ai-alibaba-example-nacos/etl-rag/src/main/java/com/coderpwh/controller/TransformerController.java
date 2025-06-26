package com.coderpwh.controller;

import com.coderpwh.model.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author coderpwh
 */
@RestController
@RequestMapping("/transformer")
public class TransformerController {
    private static final Logger logger = LoggerFactory.getLogger(TransformerController.class);

    private final List<Document> documents;

    private final ChatModel chatModel;

    public TransformerController(ChatModel chatModel) {
        logger.info("start read pdf file by page");
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        // 只可以传pdf格式文件
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource);
        this.documents = pagePdfDocumentReader.read();

        this.chatModel = chatModel;
    }


    @GetMapping("/token-text-splitter")
    public List<Document> tokenTextSplitter() {
        logger.info("start token text splitter");
        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true).build();
        return tokenTextSplitter.split(this.documents);
    }


    @GetMapping("/content-format-transformer")
    public List<Document> contentFormatTransformer() {
        DefaultContentFormatter defaultContentFormatter = DefaultContentFormatter.defaultConfig();
        ContentFormatTransformer formatTransformer = new ContentFormatTransformer(defaultContentFormatter);
        return formatTransformer.apply(this.documents);
    }

    @GetMapping("/keyword-metadata-enricher")
    public List<Document> keywordMetadataEnricher() {
        logger.info("start keyword metadata enricher");
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(this.chatModel, 3);
        return keywordMetadataEnricher.apply(this.documents);
    }

    @GetMapping("/summary-metadata-enricher")
    public List<Document> summaryMetadataEnricher() {
        logger.info("start summary metadata enricher");
        List<SummaryMetadataEnricher.SummaryType> summaryTypes = List.of(
                SummaryMetadataEnricher.SummaryType.NEXT,
                SummaryMetadataEnricher.SummaryType.CURRENT,
                SummaryMetadataEnricher.SummaryType.PREVIOUS);
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(this.chatModel, summaryTypes);
        return summaryMetadataEnricher.apply(this.documents);
    }


}
