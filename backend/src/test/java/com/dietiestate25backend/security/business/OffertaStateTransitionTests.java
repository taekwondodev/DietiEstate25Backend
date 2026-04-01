package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Offerta State Transition Validation Tests - Business Logic Security
 *
 * Testa che le transizioni di stato siano valide secondo la state machine.
 *   IN_SOSPESO → ACCETTATA  ✓
 *   IN_SOSPESO → RIFIUTATA  ✓
 *   Qualsiasi → stesso stato ✗
 *   ACCETTATA/RIFIUTATA → qualsiasi stato ✗ (terminali)
 */
@DisplayName("Offerta State Transition Tests - Business Logic Security")
class OffertaStateTransitionTests extends BaseIntegrationTest {

    @Autowired
    private OffertaService offertaService;

    @MockitoBean
    private OffertaDao offertaDao;

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
    @DisplayName("IN_SOSPESO → ACCETTATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToAccettata_ShouldBeValid() {
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Offerta offertaInSospeso = new Offerta(1, 50000.0, StatoOfferta.IN_SOSPESO, "client1", immobile);

        when(offertaDao.getOffertaById(1)).thenReturn(offertaInSospeso);
        when(offertaDao.aggiornaStatoOfferta(new Offerta(1, 50000.0, StatoOfferta.ACCETTATA, "client1", immobile)))
                .thenReturn(true);

        try {
            offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(1, "Accettata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → ACCETTATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → RIFIUTATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToRifiutata_ShouldBeValid() {
        Immobile immobile = buildTestImmobile(2, "Via Milano", "agente1");
        Offerta offertaInSospeso = new Offerta(2, 60000.0, StatoOfferta.IN_SOSPESO, "client1", immobile);

        when(offertaDao.getOffertaById(2)).thenReturn(offertaInSospeso);
        when(offertaDao.aggiornaStatoOfferta(new Offerta(2, 60000.0, StatoOfferta.RIFIUTATA, "client1", immobile)))
                .thenReturn(true);

        try {
            offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(2, "Rifiutata"), "client1");
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → RIFIUTATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("IN_SOSPESO → IN_SOSPESO - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToInSospeso_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(3, "Via Napoli", "agente1");
        Offerta offertaInSospeso = new Offerta(3, 70000.0, StatoOfferta.IN_SOSPESO, "client1", immobile);

        when(offertaDao.getOffertaById(3)).thenReturn(offertaInSospeso);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(3, "In sospeso"), "client1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_OFFERTA_STATUS;
    }

    @Test
    @DisplayName("ACCETTATA → RIFIUTATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testAccettata_ToRifiutata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(4, "Via Veneto", "agente1");
        Offerta offertaAccettata = new Offerta(4, 80000.0, StatoOfferta.ACCETTATA, "client1", immobile);

        when(offertaDao.getOffertaById(4)).thenReturn(offertaAccettata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(4, "Rifiutata"), "agente1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_OFFERTA_STATUS;
    }

    @Test
    @DisplayName("ACCETTATA → ACCETTATA - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testAccettata_ToAccettata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Offerta offertaAccettata = new Offerta(5, 90000.0, StatoOfferta.ACCETTATA, "client1", immobile);

        when(offertaDao.getOffertaById(5)).thenReturn(offertaAccettata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(5, "Accettata"), "client1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_OFFERTA_STATUS;
    }

    @Test
    @DisplayName("RIFIUTATA → ACCETTATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRifiutata_ToAccettata_ShouldBeInvalid() {
        Immobile immobile = buildTestImmobile(6, "Via Firenze", "agente1");
        Offerta offertaRifiutata = new Offerta(6, 100000.0, StatoOfferta.RIFIUTATA, "client1", immobile);

        when(offertaDao.getOffertaById(6)).thenReturn(offertaRifiutata);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(6, "Accettata"), "agente1")
        );

        assert exception.getErrorCode() == ErrorCode.INVALID_OFFERTA_STATUS;
    }
}