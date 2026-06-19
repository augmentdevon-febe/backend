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
        raw.setProvider("openai");
        raw.setModel("test-model");
        raw.setRawText("{\"predictedHomeGoals\":2,\"predictedAwayGoals\":1,\"result\":\"HOME_WIN\",\"confidenceScore\":0.72,\"explanation\":\"test\",\"factors\":{\"ranking\":0.3}}");

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(2, dto.getPredictedHomeGoals());
        Assertions.assertEquals(1, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.72, dto.getConfidence());
        Assertions.assertEquals("test", dto.getExplanation());
    }

    @Test
    public void parsesWrappedOpenAiJson() throws Exception {
        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider("openai");
        raw.setModel("test-model");
        raw.setRawText("{\"choices\":[{\"message\":{\"content\":{\"parts\":[\"{\\\"predictedHomeGoals\\\":3,\\\"predictedAwayGoals\\\":2,\\\"result\\\":\\\"HOME_WIN\\\",\\\"confidenceScore\\\":0.91,\\\"explanation\\\":\\\"wrapped response\\\",\\\"factors\\\":{\\\"ranking\\\":0.2}}\"]}}}]}");

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(3, dto.getPredictedHomeGoals());
        Assertions.assertEquals(2, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.91, dto.getConfidence());
        Assertions.assertEquals("wrapped response", dto.getExplanation());
    }

    @Test
    public void parsesOpenAiResponsesOutputText() throws Exception {
        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider("openai");
        raw.setModel("test-model");
        raw.setRawText("{\"output_text\":\"{\\\"predictedHomeGoals\\\":4,\\\"predictedAwayGoals\\\":3,\\\"result\\\":\\\"HOME_WIN\\\",\\\"confidenceScore\\\":0.88,\\\"explanation\\\":\\\"responses api output_text\\\",\\\"factors\\\":{\\\"ranking\\\":0.25}}\"}");

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(4, dto.getPredictedHomeGoals());
        Assertions.assertEquals(3, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.88, dto.getConfidence());
        Assertions.assertEquals("responses api output_text", dto.getExplanation());
    }

    @Test
    public void parsesOpenAiResponsesOutputArray() throws Exception {
        ExternalAiRawResponse raw = new ExternalAiRawResponse();
        raw.setProvider("openai");
        raw.setModel("test-model");
        raw.setRawText("{\"output\":[{\"id\":\"1\",\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":\"{\\\"predictedHomeGoals\\\":5,\\\"predictedAwayGoals\\\":2,\\\"result\\\":\\\"HOME_WIN\\\",\\\"confidenceScore\\\":0.94,\\\"explanation\\\":\\\"responses api output array\\\",\\\"factors\\\":{\\\"ranking\\\":0.2}}\"}]}]}" );

        PredictionDto dto = PredictionResponseParser.parse(raw);
        Assertions.assertEquals(5, dto.getPredictedHomeGoals());
        Assertions.assertEquals(2, dto.getPredictedAwayGoals());
        Assertions.assertEquals(0.94, dto.getConfidence());
        Assertions.assertEquals("responses api output array", dto.getExplanation());
    }
}
