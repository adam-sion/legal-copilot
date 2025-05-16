package com.example.legalcopilot.advanced;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.retriever.Retriever;
import org.springframework.ai.retriever.VectorStoreRetriever;
import org.springframework.ai.vectorstore.Filter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdvancedLegalQAService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Retriever<Document> retriever;

    public AdvancedLegalQAService(ChatClient chatClient, EmbeddingClient embeddingClient, DataSource dataSource) {
        this.chatClient = chatClient;
        
        // Create vector store
        this.vectorStore = new PgVectorStore(dataSource, embeddingClient);
        
        // Create retriever with custom configuration
        this.retriever = VectorStoreRetriever.builder(this.vectorStore)
                .withTopK(5)             // Retrieve top 5 documents
                .withSimilarityThreshold(0.7)  // Only documents with similarity score > 0.7
                .build();
    }

    /**
     * Advanced query with metadata filtering and reranking
     */
    public String advancedLegalQuery(String query, String documentType, String clientId) {
        // Step 1: Create metadata filters
        Map<String, Object> metadataFilters = new HashMap<>();
        if (documentType != null) {
            metadataFilters.put("documentType", documentType);
        }
        if (clientId != null) {
            metadataFilters.put("clientId", clientId);
        }
        
        // Step 2: Retrieve relevant documents with filtering
        List<Document> retrievedDocs = new ArrayList<>();
        if (!metadataFilters.isEmpty()) {
            Filter filter = Filter.of(metadataFilters);
            retrievedDocs = vectorStore.similaritySearch(query, 10, filter);
        } else {
            retrievedDocs = retriever.retrieve(query);
        }
        
        // Step 3: Basic reranking by extracting keyword matches
        List<Document> rankedDocs = retrievedDocs.stream()
                .sorted((d1, d2) -> {
                    // Simple keyword matching (in a real app, use a more sophisticated reranker)
                    long matches1 = countKeywordMatches(d1.getContent(), query);
                    long matches2 = countKeywordMatches(d2.getContent(), query);
                    return Long.compare(matches2, matches1);  // Higher matches first
                })
                .limit(5)  // Take top 5 after reranking
                .collect(Collectors.toList());
                
        // Step 4: Format context with document citations
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < rankedDocs.size(); i++) {
            Document doc = rankedDocs.get(i);
            String docId = doc.getMetadata().getOrDefault("documentId", "unknown").toString();
            String docType = doc.getMetadata().getOrDefault("documentType", "unknown").toString();
            
            contextBuilder.append("--- Document ").append(i + 1).append(" (ID: ")
                      .append(docId).append(", Type: ").append(docType).append(") ---\n");
            contextBuilder.append(doc.getContent()).append("\n\n");
        }
        
        String context = contextBuilder.toString();
        
        // Step 5: Create system prompt with enhanced instructions
        String systemPromptText = """
                You are a specialized legal assistant analyzing contracts and legal documents.
                
                INSTRUCTIONS:
                1. Answer ONLY based on the provided CONTEXT below.
                2. Be precise and factual, citing specific sections from the documents when possible.
                3. If the information isn't in the CONTEXT, state "I don't have enough information in the provided documents" - do NOT make up information.
                4. Make sure to address all aspects of the user's query.
                5. When referring to contract terms, specify which document contains the information.
                
                CONTEXT:
                {{context}}
                """;
        
        // Step 6: Build messages
        Message systemMessage = new SystemMessage(systemPromptText.replace("{{context}}", context));
        Message userMessage = new UserMessage(query);
        
        // Step 7: Create prompt and generate response
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        String response = chatClient.call(prompt).getResult().getOutput().getContent();
        
        return response;
    }
    
    private long countKeywordMatches(String content, String query) {
        // Simple implementation - in practice, use more sophisticated NLP techniques
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        return java.util.Arrays.stream(queryWords)
                .filter(word -> word.length() > 3) // Only consider meaningful words
                .filter(contentLower::contains)
                .count();
    }
}
