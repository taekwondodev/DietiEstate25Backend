package com.dietiestate25backend.security.business;

import com.dietiestate25backend.TestConfiguration;
import com.dietiestate25backend.model.StatoOfferta;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.dietiestate25backend.error.exception.BadRequestException;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.Immobile;

/**
 * Offerta State Transition Validation Tests
 *
 * Business Logic Security - Phase 4
 *
 * Testa che le transizioni di stato siano valide secondo la state machine.
 * Una offerta può transitare da:
 *   - IN_SOSPESO → ACCETTATA
 *   - IN_SOSPESO → RIFIUTATA
 *
 * Transizioni non permesse:
 *   - IN_SOSPESO → IN_SOSPESO (same state)
 *   - ACCETTATA → qualsiasi stato (terminale)
 *   - RIFIUTATA → qualsiasi stato (terminale)
 *
 * Scopo: Verificare che i client non possano causare transizioni di stato non valide.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Offerta State Transition Tests - Business Logic Security")
class OffertaStateTransitionTests {

    @Autowired
    private OffertaService offertaService;

    @MockitoBean
    private OffertaDao offertaDao;

    private Immobile buildTestImmobile(int id, String indirizzo, String idResponsabile) {
        return new Immobile.Builder()
                .setIdImmobile(id)
                .setIndirizzo(indirizzo)
                .setLatitudine(43.7 + id * 0.01)
                .setLongitudine(10.4 + id * 0.01)
                .setDescrizione("Test immobile")
                .setNStanze(4)
                .setDimensione(100)
                .setNBagni(2)
                .setUrlFoto("image.jpg")
                .setIdResponsabile(idResponsabile)
                .setPrezzo(100000.0)
                .setTipologia("Appartamento")
                .setComune("Genova")
                .setPiano(2)
                .setHasAscensore(true)
                .setHasBalcone(false)
                .build();
    }

    /**
     * SCENARIO 1: Transizione IN_SOSPESO → ACCETTATA
     *
     * Precondizione: Offerta nello stato IN_SOSPESO
     * Azione: Cliente tenta di passarla a ACCETTATA
     * Aspettativa: Transizione permessa, nessuna eccezione
     *
     * State Machine: IN_SOSPESO può andare a ACCETTATA ✓
     */
    @Test
    @DisplayName("IN_SOSPESO → ACCETTATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToAccettata_ShouldBeValid() {
        // Setup
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Offerta offertaInSospeso = new Offerta(
                1,
                50000.0,
                StatoOfferta.IN_SOSPESO,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(1)).thenReturn(offertaInSospeso);
        when(offertaDao.aggiornaStatoOfferta(new Offerta(1, 50000.0, StatoOfferta.ACCETTATA, "client1", immobile)))
                .thenReturn(true);

        // Act & Assert: Nessuna eccezione lanciata
        try {
            offertaService.aggiornaStatoOfferta(
                    new AggiornaOffertaRequest(1, "Accettata"),
                    "client1"
            );
            // Se arriviamo qui, la transizione è stata accettata ✓
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → ACCETTATA dovrebbe essere valida, ma è stata rifiutata: " + e.getMessage());
        }
    }

    /**
     * SCENARIO 2: Transizione IN_SOSPESO → RIFIUTATA
     *
     * Precondizione: Offerta nello stato IN_SOSPESO
     * Azione: Cliente tenta di passarla a RIFIUTATA
     * Aspettativa: Transizione permessa, nessuna eccezione
     *
     * State Machine: IN_SOSPESO può andare a RIFIUTATA ✓
     */
    @Test
    @DisplayName("IN_SOSPESO → RIFIUTATA - SHOULD be valid transition (no exception)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToRifiutata_ShouldBeValid() {
        // Setup
        Immobile immobile = buildTestImmobile(2, "Via Milano", "agente1");
        Offerta offertaInSospeso = new Offerta(
                2,
                60000.0,
                StatoOfferta.IN_SOSPESO,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(2)).thenReturn(offertaInSospeso);
        when(offertaDao.aggiornaStatoOfferta(new Offerta(2, 60000.0, StatoOfferta.RIFIUTATA, "client1", immobile)))
                .thenReturn(true);

        // Act & Assert
        try {
            offertaService.aggiornaStatoOfferta(
                    new AggiornaOffertaRequest(2, "Rifiutata"),
                    "client1"
            );
        } catch (BadRequestException e) {
            throw new AssertionError("Transizione IN_SOSPESO → RIFIUTATA dovrebbe essere valida: " + e.getMessage());
        }
    }

    /**
     * SCENARIO 3: Transizione IN_SOSPESO → IN_SOSPESO (stesso stato)
     *
     * Precondizione: Offerta nello stato IN_SOSPESO
     * Azione: Cliente tenta di passarla a IN_SOSPESO (nessun cambio)
     * Aspettativa: Transizione RIFIUTATA con BadRequestException
     *
     * Reason: Non ha senso transizionare a se stesso
     *
     * State Machine: IN_SOSPESO → IN_SOSPESO è FALSE
     */
    @Test
    @DisplayName("IN_SOSPESO → IN_SOSPESO - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testInSospeso_ToInSospeso_ShouldBeInvalid() {
        // Setup
        Immobile immobile = buildTestImmobile(3, "Via Napoli", "agente1");
        Offerta offertaInSospeso = new Offerta(
                3,
                70000.0,
                StatoOfferta.IN_SOSPESO,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(3)).thenReturn(offertaInSospeso);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(3, "In sospeso"),
                        "client1"
                ),
                "Transizione a stesso stato dovrebbe lanciare BadRequestException"
        );

        // Verifica che il messaggio sia generico e non leaki informazioni
        assert exception.getMessage().equals("Stato Offerta non valido") :
                "Messaggio di errore non dovrebbe contenere dettagli implementativi";
    }

    /**
     * SCENARIO 4: Transizione ACCETTATA → RIFIUTATA
     *
     * Precondizione: Offerta nello stato ACCETTATA (terminale)
     * Azione: Agente tenta di passarla a RIFIUTATA
     * Aspettativa: Transizione RIFIUTATA con BadRequestException
     *
     * Business Rule: Una offerta ACCETTATA è terminale, non può cambiare
     *
     * State Machine: ACCETTATA → qualsiasi stato è FALSE
     */
    @Test
    @DisplayName("ACCETTATA → RIFIUTATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testAccettata_ToRifiutata_ShouldBeInvalid() {
        // Setup
        Immobile immobile = buildTestImmobile(4, "Via Veneto", "agente1");
        Offerta offertaAccettata = new Offerta(
                4,
                80000.0,
                StatoOfferta.ACCETTATA,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(4)).thenReturn(offertaAccettata);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(4, "Rifiutata"),
                        "agente1"
                ),
                "Transizione da ACCETTATA dovrebbe lanciare BadRequestException"
        );

        // Verifica messaggio generico
        assert exception.getMessage().equals("Stato Offerta non valido");
    }

    /**
     * SCENARIO 5: Transizione ACCETTATA → ACCETTATA (stesso stato terminale)
     *
     * Precondizione: Offerta nello stato ACCETTATA
     * Azione: Cliente tenta di passarla a ACCETTATA (nessun cambio)
     * Aspettativa: Transizione RIFIUTATA con BadRequestException
     */
    @Test
    @DisplayName("ACCETTATA → ACCETTATA - SHOULD be rejected (same state)")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testAccettata_ToAccettata_ShouldBeInvalid() {
        // Setup
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Offerta offertaAccettata = new Offerta(
                5,
                90000.0,
                StatoOfferta.ACCETTATA,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(5)).thenReturn(offertaAccettata);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(5, "Accettata"),
                        "client1"
                )
        );

        assert exception.getMessage().equals("Stato Offerta non valido");
    }

    /**
     * SCENARIO 6: Transizione RIFIUTATA → ACCETTATA
     *
     * Precondizione: Offerta nello stato RIFIUTATA (terminale)
     * Azione: Agente tenta di passarla a ACCETTATA
     * Aspettativa: Transizione RIFIUTATA con BadRequestException
     *
     * Business Rule: Una offerta RIFIUTATA è terminale
     */
    @Test
    @DisplayName("RIFIUTATA → ACCETTATA - SHOULD be rejected (terminal state)")
    @WithMockUser(username = "agente1", roles = "AgenteImmobiliare")
    void testRifiutata_ToAccettata_ShouldBeInvalid() {
        // Setup
        Immobile immobile = buildTestImmobile(6, "Via Firenze", "agente1");
        Offerta offertaRifiutata = new Offerta(
                6,
                100000.0,
                StatoOfferta.RIFIUTATA,
                "client1",
                immobile
        );

        when(offertaDao.getOffertaById(6)).thenReturn(offertaRifiutata);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(6, "Accettata"),
                        "agente1"
                ),
                "Transizione da RIFIUTATA dovrebbe lanciare BadRequestException"
        );

        assert exception.getMessage().equals("Stato Offerta non valido");
    }
}
