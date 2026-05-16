package com.ai.platform.logaiplatform.dto;


import com.ai.platform.logaiplatform.entity.AiAlertDocument;
import lombok.Data;
import java.util.List;

@Data
public class AiAlertResponse {

    private List<AiAlertDocument> logs;
}
