package com.dietiestate25backend.security.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.dietiestate25backend.TestConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Malformed Payload Tests - Request Integrity")
class MalformedPayloadTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Malformed JSON - Invalid JSON syntax should return 400 Bad Request")
    void testMalformedJson_InvalidSyntax_ShouldReturn400() throws Exception {
        String invalidJson = "{email: 'user@example.com', password: 'password'}"; // Missing quotes

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Empty Body - POST with empty body should return 400 Bad Request")
    void testEmptyBody_PostWithEmptyBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Empty Object - POST with empty JSON object should return 400 Bad Request")
    void testEmptyObject_PostWithEmptyJsonObject_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Wrong Content Type - Text/plain should not be accepted for endpoints expecting JSON")
    void testWrongContentType_TextPlainShouldNotBeAccepted() throws Exception {
        String payload = "email=user@example.com&password=password";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Huge Payload - Extremely large JSON should be rejected")
    void testHugePayload_ExtremelyLargeShouldBeRejected() throws Exception {
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            largeString.append("x");
        }
        String hugeJson = "{\"email\":\"" + largeString.toString() + "@example.com\",\"password\":\"password\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(hugeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Null JSON - Sending 'null' as body should return 400 Bad Request")
    void testNullJson_SendingNullAsBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Array instead of Object - Sending array instead of object should return 400 Bad Request")
    void testArrayInsteadOfObject_ShouldReturn400() throws Exception {
        String jsonArray = "[{\"email\":\"user@example.com\",\"password\":\"password\"}]";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonArray))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Negative Integer Parameters - Negative page/size should be handled properly")
    void testNegativeIntegerParameters_ShouldBeHandled() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli")
                .param("page", "-1")
                .param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Non-Integer Parameters - Non-numeric values for int params should be rejected")
    void testNonIntegerParameters_ShouldBeRejected() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli")
                .param("page", "not-a-number")
                .param("size", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Double Parameters - Invalid double format should be rejected")
    void testDoubleParameters_InvalidFormatShouldBeRejected() throws Exception {
        mockMvc.perform(get("/immobile/cerca")
                .param("comune", "Napoli")
                .param("prezzoMin", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Special Characters in String - Should not cause injection")
    void testSpecialCharactersInString_ShouldNotCauseInjection() throws Exception {
        String jsonWithSpecialChars = "{\"email\":\"user'; DROP TABLE--@example.com\",\"password\":\"password\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithSpecialChars))
                .andExpect(status().isBadRequest()); // Invalid email format
    }

    @Test
    @DisplayName("Unicode Characters - Valid unicode should be accepted")
    void testUnicodeCharacters_ValidUnicodeShouldBeAccepted() throws Exception {
        String jsonWithUnicode = "{\"email\":\"user@example.com\",\"password\":\"пароль123\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithUnicode))
                .andExpect(status().isUnauthorized()); // Not a validation error
    }
}

