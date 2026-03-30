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

import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.Immobile;

/**
 * Offerta Exception Handling Tests
 *
 * Business Logic Security - Phase 4
 *
 * Verifica che il service lanci le eccezioni corrette quando:
 * - Un utente tenta di modificare offerta non sua
 * - Un utente tenta di accedere a offerta inesistente
 * - Transizioni di stato non valide vengono rifiutate
 * - Le guardie di sicurezza vengono applicate nell'ordine corretto
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Offerta Exception Handling Tests - Business Logic Security")
class OffertaExceptionHandlingTests {

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
     * Client1 tenta di modificare offerta creata da Client2.
     *
     * Precondizione: Offerta appartiene a client2-uid
     * Azione: client1-uid chiama aggiornaStatoOfferta()
     * Aspettativa: UnauthorizedException lanciato
     */
    @Test
    @DisplayName("Client1 modifica offerta di Client2 - SHOULD throw UnauthorizedException")
    @WithMockUser(username = "client1-uid", roles = "Cliente")
    void testClienteModificaOffertaAltrui_ShouldThrowUnauthorizedException() {
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Offerta offertaClient2 = new Offerta(
                1,
                50000.0,
                StatoOfferta.IN_SOSPESO,
                "client2-uid",
                immobile
        );

        when(offertaDao.getOffertaById(1)).thenReturn(offertaClient2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Accettata"),
                        "client1-uid"
                ),
                "Client1 dovrebbe ricevere UnauthorizedException"
        );

        assertEquals(
                "Utente non autorizzato",
                exception.getMessage()
        );
    }

    /**
     * Agente1 tenta di modificare offerta su immobile gestito da Agente2.
     *
     * Precondizione: Offerta su immobile responsabile da agente2-uid
     * Azione: agente1-uid chiama aggiornaStatoOfferta()
     * Aspettativa: UnauthorizedException lanciato
     */
    @Test
    @DisplayName("Agente1 modifica offerta su immobile di Agente2 - SHOULD throw UnauthorizedException")
    @WithMockUser(username = "agente1-uid", roles = "AgenteImmobiliare")
    void testAgenteModificaOffertaCollega_ShouldThrowUnauthorizedException() {
        Immobile immobileAgente2 = buildTestImmobile(2, "Via Milano", "agente2-uid");
        Offerta offertaImmobileAgente2 = new Offerta(
                2,
                60000.0,
                StatoOfferta.IN_SOSPESO,
                "client1",
                immobileAgente2
        );

        when(offertaDao.getOffertaById(2)).thenReturn(offertaImmobileAgente2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(2, "Rifiutata"),
                        "agente1-uid"
                ),
                "Agente1 dovrebbe ricevere UnauthorizedException"
        );

        assertEquals(
                "Utente non autorizzato",
                exception.getMessage()
        );
    }

    /**
     * Cliente tenta di modificare offerta che non esiste.
     *
     * Precondizione: Offerta con ID 9999 non esiste
     * Azione: client1 chiama aggiornaStatoOfferta(9999, ...)
     * Aspettativa: NotFoundException lanciato
     */
    @Test
    @DisplayName("Cliente modifica offerta inesistente - SHOULD throw NotFoundException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testClienteModificaOffertaInesistente_ShouldThrowNotFoundException() {
        when(offertaDao.getOffertaById(9999)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(9999, "Accettata"),
                        "client1"
                ),
                "NotFoundException dovrebbe essere lanciato per offerta inesistente"
        );

        assertEquals(
                "Offerta non trovato",
                exception.getMessage()
        );
    }

    /**
     * DAO lancia eccezione inaspettata durante operazione.
     *
     * Precondizione: DAO throws RuntimeException
     * Azione: aggiornaStatoOfferta() chiama DAO
     * Aspettativa: InternalServerErrorException lanciato
     */
    @Test
    @DisplayName("DAO throws unexpected exception - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testDAOThrowsException_ShouldBeWrappedInGenericException() {
        when(offertaDao.getOffertaById(1))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(
                InternalServerErrorException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(1, "Accettata"),
                        "client1"
                ),
                "Unexpected exceptions should be wrapped"
        );
    }

    /**
     * Guardia di ownership viene controllata PRIMA della validazione di stato.
     *
     * Precondizione: Offerta di client2 in stato ACCETTATA (transizione invalida)
     * Azione: client1 tenta di modificarla a RIFIUTATA
     * Aspettativa: UnauthorizedException lanciato (non BadRequestException)
     */
    @Test
    @DisplayName("Ownership check BEFORE state validation - SHOULD throw UnauthorizedException first")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testOwnershipCheckBeforeStateValidation() {
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Offerta offertaClient2Accettata = new Offerta(
                5,
                100000.0,
                StatoOfferta.ACCETTATA,
                "client2-uid",
                immobile
        );

        when(offertaDao.getOffertaById(5)).thenReturn(offertaClient2Accettata);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(
                        new AggiornaOffertaRequest(5, "Rifiutata"),
                        "client1"
                ),
                "Ownership check deve lanciare prima della validazione di stato"
        );

        assertEquals(
                "Utente non autorizzato",
                exception.getMessage()
        );
    }
}
