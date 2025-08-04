package com.coderpwh.controller;

import com.coderpwh.model.Constant;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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
@RequestMapping("/reader")
public class ReaderController {

    private static final Logger logger = LoggerFactory.getLogger(ReaderController.class);

    @GetMapping("/text")
    public List<Document> readText() {
        logger.info("开始读取文本文件...");
        Resource resource = new DefaultResourceLoader().getResource(Constant.TEXT_FILE_PATH);
        TextReader textReader = new TextReader(resource);
        return textReader.read();
    }


    @GetMapping("/json")
    public List<Document> readJson() {
        logger.info("开始读取json文件...");
        Resource resource = new DefaultResourceLoader().getResource(Constant.JSON_FILE_PATH);
        TextReader textReader = new TextReader(resource);
        return textReader.read();
    }


    @GetMapping("/pdf-page")
    public List<Document> readPdfPage() {
        logger.info("开始读取pdf文件...");
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(resource);
        return pdfDocumentReader.read();
    }

    @GetMapping("/pdf-paragraph")
    public List<Document> readPdfParagraph() {
        logger.info("start read pdf file by paragraph");
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        ParagraphPdfDocumentReader paragraphPdfDocumentReader = new ParagraphPdfDocumentReader(resource);
        return paragraphPdfDocumentReader.read();
    }

    @GetMapping("/markdown")
    public List<Document> readMarkdown() {
        // 只可以传markdown格式文件
        MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(Constant.MARKDOWN_FILE_PATH);
        return markdownDocumentReader.read();
    }

    @GetMapping("/html")
    public List<Document> readHtml() {
        logger.info("start read html file");
        Resource resource = new DefaultResourceLoader().getResource(Constant.HTML_FILE_PATH);
        JsoupDocumentReader jsoupDocumentReader = new JsoupDocumentReader(resource);
        return jsoupDocumentReader.read();
    }

    @GetMapping("/tika")
    public List<Document> readTika() {
        logger.info("start read file with Tika");
        Resource resource = new DefaultResourceLoader().getResource(Constant.HTML_FILE_PATH);
        // 可以传多种文档格式
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        return tikaDocumentReader.read();
    }




}
