package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import com.dietiestate25backend.service.VisitaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Visita State Transition Validation Tests - Business Logic Security
 *
 * Testa che le transizioni di stato siano valide secondo la state machine.
 *   IN_SOSPESO → CONFERMATA  ✓
 *   IN_SOSPESO → RIFIUTATA   ✓
 *   Qualsiasi → stesso stato  ✗
 *   CONFERMATA/RIFIUTATA → qualsiasi stato ✗ (terminali)
 */
@DisplayName("Visita State Transition Tests - Business Logic Security")
class VisitaStateTransitionTests extends BaseIntegrationTest {

    @Autowired
    private VisitaService visitaService;

    @MockitoBean
    private VisitaDao visitaDao;

    private void mockJwtUser(String uid, String role) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", uid)
                .claim("role", role)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Immobile buildTestImmobile(int id, String indirizzo, String idResponsabile) {
        return Immobile.builder()
                .idImmobile(id).indirizzo(indirizzo)
                .latitudine(43.7 + id * 0.01).longitudine(10.4 + id * 0.01)
                .descrizione("Test immobile").nStanze(4).dimensione(100).nBagni(2)
                .urlFoto("image.jpg").idResponsabile(idResponsabile)
                .prezzo(100000.0).tipologia("Appartamento").comune("Genova")
                .piano(2).hasAscensore(true).hasBalcone(false).build();
    }

    @Test
    @DisplayName("IN_SOSPESO → CONFERMATA - SHOULD be valid transition (no exception)")
    void testInSospeso_ToConfermata_ShouldBeValid() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Visita visitaInSospeso = new Visita(1, Date.valueOf("2026-04-10"), Time.valueOf("14:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(1)).thenReturn(visitaInSospeso);
        when(visitaDao.aggiornaStato(any())).thenReturn(true);

        try {
            visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → CONFERMATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → RIFIUTATA - SHOULD be valid transition (no exception)")
    void testInSospeso_ToRifiutata_ShouldBeValid() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile(2, "Via Milano", "agente1");
        Visita visitaInSospeso = new Visita(2, Date.valueOf("2026-04-11"), Time.valueOf("15:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(2)).thenReturn(visitaInSospeso);
        when(visitaDao.aggiornaStato(any())).thenReturn(true);

        try {
            visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(2, "Rifiutata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → RIFIUTATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → IN_SOSPESO - SHOULD be rejected (same state)")
    void testInSospeso_ToInSospeso_ShouldBeInvalid() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile(3, "Via Napoli", "agente1");
        Visita visitaInSospeso = new Visita(3, Date.valueOf("2026-04-12"), Time.valueOf("16:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(3)).thenReturn(visitaInSospeso);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(3, "In sospeso"), "client1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_STATUS;
    }

    @Test
    @DisplayName("CONFERMATA → RIFIUTATA - SHOULD be rejected (terminal state)")
    void testConfermata_ToRifiutata_ShouldBeInvalid() {
        mockJwtUser("agente1", "AgenteImmobiliare");
        Immobile immobile = buildTestImmobile(4, "Via Veneto", "agente1");
        Visita visitaConfermata = new Visita(4, Date.valueOf("2026-04-13"), Time.valueOf("17:00:00"),
                StatoVisita.CONFERMATA, "client1", immobile);

        when(visitaDao.getVisitaById(4)).thenReturn(visitaConfermata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(4, "Rifiutata"), "agente1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_STATUS;
    }

    @Test
    @DisplayName("CONFERMATA → CONFERMATA - SHOULD be rejected (same state)")
    void testConfermata_ToConfermata_ShouldBeInvalid() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Visita visitaConfermata = new Visita(5, Date.valueOf("2026-04-14"), Time.valueOf("18:00:00"),
                StatoVisita.CONFERMATA, "client1", immobile);

        when(visitaDao.getVisitaById(5)).thenReturn(visitaConfermata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(5, "Confermata"), "client1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_STATUS;
    }

    @Test
    @DisplayName("RIFIUTATA → CONFERMATA - SHOULD be rejected (terminal state)")
    void testRifiutata_ToConfermata_ShouldBeInvalid() {
        mockJwtUser("agente1", "AgenteImmobiliare");
        Immobile immobile = buildTestImmobile(6, "Via Firenze", "agente1");
        Visita visitaRifiutata = new Visita(6, Date.valueOf("2026-04-15"), Time.valueOf("19:00:00"),
                StatoVisita.RIFIUTATA, "client1", immobile);

        when(visitaDao.getVisitaById(6)).thenReturn(visitaRifiutata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(6, "Confermata"), "agente1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_STATUS;
    }
}