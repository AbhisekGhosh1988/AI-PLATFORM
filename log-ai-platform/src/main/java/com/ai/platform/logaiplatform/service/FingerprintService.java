package com.ai.platform.logaiplatform.service;

import com.ai.platform.logaiplatform.entity.LogDocument;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FingerprintService {

    private static final Pattern EXCEPTION_PATTERN =
            Pattern.compile("([a-zA-Z0-9_$.]+(?:Exception|Error))");
    public String generate(LogDocument log) {
        String message = normalize(log.getMessage());
        return log.getService() + ":" + extractException(log) + ":" + message;
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\d+", "").
                replaceAll("\\s+", "_").toLowerCase();
    }

    private String extractException(LogDocument log) {
        String stacktrace = log.getStacktrace();
        if (stacktrace != null) {
            Matcher matcher = EXCEPTION_PATTERN.matcher(stacktrace);
            if (matcher.find()) {
                String full = matcher.group(1);
                int lastDot = full.lastIndexOf(".");
                return lastDot >= 0 ? full.substring(lastDot + 1) : full;
            }
        }
        return "UnknownException";
    }
}
