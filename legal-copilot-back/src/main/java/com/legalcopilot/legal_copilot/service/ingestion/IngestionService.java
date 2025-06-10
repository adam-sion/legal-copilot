package com.legalcopilot.legal_copilot.service.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentProcessor documentProcessor;

    public void processFiles(List<MultipartFile> files) {
        files.stream()
                .filter(file-> !file.isEmpty())
                .forEach(documentProcessor::processFile);
    }

}
