package com.legalcopilot.legal_copilot.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentEmbeddingService {

    private final VectorStore vectorStore;

    private final Tika tika = new Tika();

    private final TextSplitter textSplitter = new TokenTextSplitter();

    public void processFiles(List<MultipartFile> files) {

        for (MultipartFile file : files) {
            try {
                String rawText = tika.parseToString(file.getInputStream());
                String filename = file.getOriginalFilename();

                Document document = new Document(rawText);
                document.getMetadata().put("filename", filename);
                document.getMetadata().put("contentType", file.getContentType());
                List<Document> documents = textSplitter.apply(document);
                vectorStore.add(documents);

                log.info("Processed and stored {} chunks from {}", documents.size(), filename);

            } catch (IOException | TikaException | SAXException e) {
                log.error("Failed to process file: {}", file.getOriginalFilename(), e);
            }
        }
    }
}