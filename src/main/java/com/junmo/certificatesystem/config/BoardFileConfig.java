package com.junmo.certificatesystem.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.junmo.certificatesystem.config.properties.BoardFileProperties;

@Configuration
@EnableConfigurationProperties(BoardFileProperties.class)
public class BoardFileConfig {
}
