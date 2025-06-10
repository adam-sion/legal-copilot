package com.legalcopilot.legal_copilot.service.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessor {

    private final VectorStore vectorStore;

    private final Tika tika = new Tika();

    private final TextSplitter textSplitter = new TokenTextSplitter();

    @Async
    public void processFile(MultipartFile file) {
        String filename = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileBytes = inputStream.readAllBytes();
            String contentType = tika.detect(fileBytes);
            String extractedText = tika.parseToString(new ByteArrayInputStream(fileBytes));

            if (extractedText != null && !extractedText.trim().isEmpty()) {
                Document document = new Document(extractedText);
                document.getMetadata().put("filename", filename);
                document.getMetadata().put("contentType", contentType);
                List<Document> chunks = textSplitter.apply(List.of(document));
                vectorStore.add(chunks);
                log.info("Stored {} chunks from '{}'", chunks.size(), filename);
            } else {
                log.warn("File '{}' extracted no text, skipping.", filename);
            }
        } catch (IOException | TikaException e) {
            log.warn("Failed to process file '{}': {}", filename, e.getMessage(), e);
        }
    }

}
