package com.dietiestate25backend;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {
    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
