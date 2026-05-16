package com.ai.platform.logaiplatform.dto;

import lombok.AllArgsConstructor;

import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceDependency {

    private String from;

    private String to;

    private Long count;
}