package org.ecews.biometricapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ecews.biometricapp.repositories.ReportRepository;
import org.ecews.biometricapp.services.ReportService;
import org.ecews.biometricapp.utils.DeDuplicationConfigs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@Slf4j
public class ReportServiceTest {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSummaryReport () {
        var report = reportService.generateSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
        log.info("Report generated successfully ************** {}", report.toString());
    }

    @Test
    void testSummaryRepository () {
        var data = reportRepository.getSummaryReport(DeDuplicationConfigs.RECAPTURE_TWO_AND_ONE);
        log.info("Data size is ******* {}", data.size());
    }

    public static Map<String, Object> convertToMap(String input) {
        // Remove leading and trailing braces
        input = input.substring(1, input.length() - 1);

        // Split input string by comma and space
        String[] keyValuePairs = input.split(",\\s+");

        // Create a Map to hold key-value pairs
        Map<String, Object> map = new HashMap<>();
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            String key = entry[0];
            String value = entry[1];

            // Convert string value to appropriate types
            Object parsedValue;
            if (value.equals("null")) {
                parsedValue = null;
            } else if (value.matches("\\d+")) {
                parsedValue = Integer.parseInt(value);
            } else {
                parsedValue = LocalDate.parse(value);
            }

            // Add key-value pair to the map
            map.put(key, parsedValue);
        }

        return map;
    }

    public Map<String, Object> convertAndSortJsonNodeKeys(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;

            // Use TreeMap to sort the keys
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            Map<String, JsonNode> sortedFields = new TreeMap<>();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                sortedFields.put(entry.getKey(), entry.getValue());
            }

            // Convert sortedFields to Map<String, Object>
            Map<String, Object> resultMap = new TreeMap<>();
            sortedFields.forEach((key, value) -> {
                if (value.isValueNode()) {
                    resultMap.put(key, objectMapper.convertValue(value, Object.class));
                } else {
                    resultMap.put(key, objectMapper.convertValue(value, Map.class));
                }
            });

            return resultMap;
        } else {
            throw new IllegalArgumentException("JsonNode is not an ObjectNode");
        }
    }
}
