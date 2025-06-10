package com.legalcopilot.legal_copilot.service.query;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatQueryService {

    private static final String LEGAL_PROMPT_TEMPLATE = """
            You are a specialized legal assistant with expertise in contract analysis and legal document interpretation.
            
            CRITICAL INSTRUCTIONS:
            1. Use the information from the PROVIDED CONTEXT below, try to use trusted external resources
            2. Be precise, factual, and cite specific document sections, clauses, or page numbers when available
            3. If information is not in the CONTEXT, explicitly state: "The provided documents do not contain sufficient information to answer this query"
            4. Address ALL aspects of the user's question systematically
            5. When referencing contract terms, always specify:
               - Document name/title
               - Section/clause number if available
               - Relevant page numbers if applicable
            6. Use professional legal terminology but explain complex concepts clearly
            7. Highlight any potential risks, obligations, or important deadlines mentioned in the documents
            8. If multiple documents contain relevant information, compare and contrast them
            
            RESPONSE FORMAT:
            - Start with a direct answer to the main query
            - Provide supporting details with specific citations
            - End with any relevant warnings or considerations
            
            Context from legal documents:
            ---------------------
            <question_answer_context>
            ---------------------
            
            User Query: <query>
            """;

    private static final PromptTemplate legalPromptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder()
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .template(LEGAL_PROMPT_TEMPLATE)
            .build();


    private final ChatClient chatClient;

    private final Advisor advisor;

    public ChatQueryService(VectorStore vectorStore, ChatClient.Builder builder) {
        this.advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(legalPromptTemplate)
                .build();
        this.chatClient = builder.build();
    }

    public String processQuery(String query) {
        String response = chatClient.prompt(query)
                .advisors(advisor)
                .call()
                .content();

        return response;
    }

}
