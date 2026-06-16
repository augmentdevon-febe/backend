package com.example.worldcuppredictor;

import com.example.worldcuppredictor.api.dto.response.ExternalAiRawResponse;
import com.example.worldcuppredictor.api.dto.response.PredictionDto;
import com.example.worldcuppredictor.infrastructure.ai.PredictionResponseParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PredictionResponseParserTest {
    @Test
    public void parsesSimpleJson() throws Exception {
        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider("m365-copilot");
        raw.setModel("test-model");
        raw.setRawText("{\"predictedHomeGoals\":2,\"predictedAwayGoals\":1,\"result\":\"HOME_WIN\",\"confidenceScore\":0.72,\"explanation\":\"test\",\"factors\":{\"ranking\":0.3}}");

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(2, dto.getPredictedHomeGoals());
        Assertions.assertEquals(1, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.72, dto.getConfidence());
        Assertions.assertEquals("test", dto.getExplanation());
    }

    @Test
    public void parsesWrappedM365CopilotJson() throws Exception {
        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider("m365-copilot");
        raw.setModel("test-model");
        raw.setRawText("{\"choices\":[{\"message\":{\"content\":{\"parts\":[\"{\\\"predictedHomeGoals\\\":3,\\\"predictedAwayGoals\\\":2,\\\"result\\\":\\\"HOME_WIN\\\",\\\"confidenceScore\\\":0.91,\\\"explanation\\\":\\\"wrapped response\\\",\\\"factors\\\":{\\\"ranking\\\":0.2}}\"]}}}]}");

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(3, dto.getPredictedHomeGoals());
        Assertions.assertEquals(2, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.91, dto.getConfidence());
        Assertions.assertEquals("wrapped response", dto.getExplanation());
    }
}
