package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.dto.requests.AggiornaVisitaRequest;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Visita Exception Handling Tests - Business Logic Security
 *
 * Verifica che il service lanci le eccezioni corrette quando:
 * - Un cliente tenta di modificare visita non sua
 * - Un agente tenta di accedere a visita su immobile non suo
 * - Una visita inesistente viene richiesta
 * - Le guardie di sicurezza vengono applicate nell'ordine corretto
 */
@DisplayName("Visita Exception Handling Tests - Business Logic Security")
class VisitaExceptionHandlingTests extends BaseIntegrationTest {

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
    @DisplayName("Client1 modifica visita di Client2 - SHOULD throw UnauthorizedException")
    @WithMockUser(username = "client1-uid", roles = "Cliente")
    void testClienteModificaVisitaAltrui_ShouldThrowUnauthorizedException() {
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Visita visitaClient2 = new Visita(1, Date.valueOf("2026-04-10"), Time.valueOf("14:00:00"),
                StatoVisita.IN_SOSPESO, "client2-uid", immobile);

        when(visitaDao.getVisitaById(1)).thenReturn(visitaClient2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), "client1-uid")
        );

        assertEquals("Non hai i permessi per aggiornare questa visita", exception.getMessage());
    }

    @Test
    @DisplayName("Agente1 modifica visita su immobile di Agente2 - SHOULD throw UnauthorizedException")
    @WithMockUser(username = "agente1-uid", roles = "AgenteImmobiliare")
    void testAgenteModificaVisitaCollega_ShouldThrowUnauthorizedException() {
        Immobile immobileAgente2 = buildTestImmobile(2, "Via Milano", "agente2-uid");
        Visita visitaImmobileAgente2 = new Visita(2, Date.valueOf("2026-04-11"), Time.valueOf("15:00:00"),
                StatoVisita.IN_SOSPESO, "client1", immobileAgente2);

        when(visitaDao.getVisitaById(2)).thenReturn(visitaImmobileAgente2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(2, "Confermata"), "agente1-uid")
        );

        assertEquals("Non hai i permessi per aggiornare questa visita", exception.getMessage());
    }

    @Test
    @DisplayName("Cliente modifica visita inesistente - SHOULD throw NotFoundException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testClienteModificaVisitaInesistente_ShouldThrowNotFoundException() {
        when(visitaDao.getVisitaById(9999)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(9999, "Confermata"), "client1")
        );

        assertEquals("Visita non trovato", exception.getMessage());
    }

    @Test
    @DisplayName("DAO throws unexpected exception - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testDAOThrowsException_ShouldBeWrappedInGenericException() {
        when(visitaDao.getVisitaById(1)).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(
                InternalServerErrorException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(1, "Confermata"), "client1")
        );
    }

    @Test
    @DisplayName("Ownership check BEFORE state validation - SHOULD throw UnauthorizedException first")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testOwnershipCheckBeforeStateValidation() {
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Visita visitaClient2Confermata = new Visita(5, Date.valueOf("2026-04-14"), Time.valueOf("18:00:00"),
                StatoVisita.CONFERMATA, "client2-uid", immobile);

        when(visitaDao.getVisitaById(5)).thenReturn(visitaClient2Confermata);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> visitaService.aggiornaStatoVisita(new AggiornaVisitaRequest(5, "Rifiutata"), "client1")
        );

        assertEquals("Non hai i permessi per aggiornare questa visita", exception.getMessage());
    }
}