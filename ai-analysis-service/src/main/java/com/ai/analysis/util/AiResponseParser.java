package com.ai.analysis.util;

import com.ai.analysis.entity.AiAlertDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AiResponseParser {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public static List<AiAlertDocument> parse(String response) {

        try {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");

            if (start == -1 || end == -1) {
                throw new RuntimeException("No JSON found in AI response");
            }
            String json = response.substring(start, end + 1);
            // Remove comments if AI adds them
            json = json.replaceAll("//.*", "");
            // Convert JSON -> Entity
            AiAlertDocument alert = mapper.readValue(json, AiAlertDocument.class);
            return Collections.singletonList(alert);

        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", response, ex);
            throw new RuntimeException("Failed to parse AI response", ex);
        }
    }
}