package com.dietiestate25backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfiguration.class)
class DietiEstate25BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
