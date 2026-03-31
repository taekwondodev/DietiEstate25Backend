package com.dietiestate25backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfiguration.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
}