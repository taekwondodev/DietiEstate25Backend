package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import com.dietiestate25backend.service.VisitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private Immobile buildTestImmobile(int id, String indirizzo, String idResponsabile) {
        return new Immobile.Builder()
                .setIdImmobile(id).setIndirizzo(indirizzo)
                .setLatitudine(43.7 + id * 0.01).setLongitudine(10.4 + id * 0.01)
                .setDescrizione("Test immobile").setNStanze(4).setDimensione(100).setNBagni(2)
                .setUrlFoto("image.jpg").setIdResponsabile(idResponsabile)
                .setPrezzo(100000.0).setTipologia("Appartamento").setComune("Genova")
                .setPiano(2).setHasAscensore(true).setHasBalcone(false).build();
    }

    @Test
    @DisplayName("IN_SOSPESO → CONFERMATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToConfermata_ShouldBeValid() {
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Visita visitaInSospeso = new Visita(1, Date.valueOf("2026-04-10"), Time.valueOf("14:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(1)).thenReturn(visitaInSospeso);
        when(visitaDao.aggiornaStato(new Visita(1, visitaInSospeso.getDataVisita(),
                visitaInSospeso.getOraVisita(), StatoVisita.CONFERMATA, "client1", immobile)))
                .thenReturn(true);

        try {
            visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → CONFERMATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → RIFIUTATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToRifiutata_ShouldBeValid() {
        Immobile immobile = buildTestImmobile(2, "Via Milano", "agente1");
        Visita visitaInSospeso = new Visita(2, Date.valueOf("2026-04-11"), Time.valueOf("15:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(2)).thenReturn(visitaInSospeso);
        when(visitaDao.aggiornaStato(new Visita(2, visitaInSospeso.getDataVisita(),
                visitaInSospeso.getOraVisita(), StatoVisita.RIFIUTATA, "client1", immobile)))
                .thenReturn(true);

        try {
            visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(2, "Rifiutata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → RIFIUTATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → IN_SOSPESO - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToInSospeso_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(3, "Via Napoli", "agente1");
        Visita visitaInSospeso = new Visita(3, Date.valueOf("2026-04-12"), Time.valueOf("16:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobile);

        when(visitaDao.getVisitaById(3)).thenReturn(visitaInSospeso);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(3, "In sospeso"), "client1")
        );

        assert exception.getMessage().equals("Stato non valido");
    }

    @Test
    @DisplayName("CONFERMATA → RIFIUTATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testConfermata_ToRifiutata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(4, "Via Veneto", "agente1");
        Visita visitaConfermata = new Visita(4, Date.valueOf("2026-04-13"), Time.valueOf("17:00:00"),
                StatoVisita.CONFERMATA, "client1", immobile);

        when(visitaDao.getVisitaById(4)).thenReturn(visitaConfermata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(4, "Rifiutata"), "agente1")
        );

        assert exception.getMessage().equals("Stato non valido");
    }

    @Test
    @DisplayName("CONFERMATA → CONFERMATA - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testConfermata_ToConfermata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Visita visitaConfermata = new Visita(5, Date.valueOf("2026-04-14"), Time.valueOf("18:00:00"),
                StatoVisita.CONFERMATA, "client1", immobile);

        when(visitaDao.getVisitaById(5)).thenReturn(visitaConfermata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(5, "Confermata"), "client1")
        );

        assert exception.getMessage().equals("Stato non valido");
    }

    @Test
    @DisplayName("RIFIUTATA → CONFERMATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRifiutata_ToConfermata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(6, "Via Firenze", "agente1");
        Visita visitaRifiutata = new Visita(6, Date.valueOf("2026-04-15"), Time.valueOf("19:00:00"),
                StatoVisita.RIFIUTATA, "client1", immobile);

        when(visitaDao.getVisitaById(6)).thenReturn(visitaRifiutata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(6, "Confermata"), "agente1")
        );

        assert exception.getMessage().equals("Stato non valido");
    }
}