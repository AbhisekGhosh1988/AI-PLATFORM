package com.ai.platform.logaiplatform.entity;

import lombok.Data;

import java.time.Instant;

import java.util.List;

@Data
public class VectorLogDocument {

    private String message;

    private String service;

    private Instant timestamp;

    private List<Double> embedding;
}