package com.dietiestate25backend.security.business;

import com.dietiestate25backend.BaseIntegrationTest;
import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.dto.requests.AggiornaOffertaRequest;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.InternalServerErrorException;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import com.dietiestate25backend.service.OffertaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Offerta Exception Handling Tests - Business Logic Security
 *
 * Verifica che il service lanci le eccezioni corrette quando:
 * - Un utente tenta di modificare offerta non sua
 * - Un utente tenta di accedere a offerta inesistente
 * - Le guardie di sicurezza vengono applicate nell'ordine corretto
 */
@DisplayName("Offerta Exception Handling Tests - Business Logic Security")
class OffertaExceptionHandlingTests extends BaseIntegrationTest {

    @Autowired
    private OffertaService offertaService;

    @MockitoBean
    private OffertaDao offertaDao;

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
        return new Immobile.Builder()
                .setIdImmobile(id).setIndirizzo(indirizzo)
                .setLatitudine(43.7 + id * 0.01).setLongitudine(10.4 + id * 0.01)
                .setDescrizione("Test immobile").setNStanze(4).setDimensione(100).setNBagni(2)
                .setUrlFoto("image.jpg").setIdResponsabile(idResponsabile)
                .setPrezzo(100000.0).setTipologia("Appartamento").setComune("Genova")
                .setPiano(2).setHasAscensore(true).setHasBalcone(false).build();
    }

    @Test
    @DisplayName("Client1 modifica offerta di Client2 - SHOULD throw UnauthorizedException")
    void testClienteModificaOffertaAltrui_ShouldThrowUnauthorizedException() {
        mockJwtUser("client1-uid", "Cliente");
        Immobile immobile = buildTestImmobile(1, "Via Roma", "agente1");
        Offerta offertaClient2 = new Offerta(1, 50000.0, StatoOfferta.IN_SOSPESO, "client2-uid", immobile);

        when(offertaDao.getOffertaById(1)).thenReturn(offertaClient2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(1, "Accettata"), "client1-uid")
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Agente1 modifica offerta su immobile di Agente2 - SHOULD throw UnauthorizedException")
    void testAgenteModificaOffertaCollega_ShouldThrowUnauthorizedException() {
        mockJwtUser("agente1-uid", "AgenteImmobiliare");
        Immobile immobileAgente2 = buildTestImmobile(2, "Via Milano", "agente2-uid");
        Offerta offertaImmobileAgente2 = new Offerta(2, 60000.0, StatoOfferta.IN_SOSPESO, "client1", immobileAgente2);

        when(offertaDao.getOffertaById(2)).thenReturn(offertaImmobileAgente2);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(2, "Rifiutata"), "agente1-uid")
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Cliente modifica offerta inesistente - SHOULD throw NotFoundException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testClienteModificaOffertaInesistente_ShouldThrowNotFoundException() {
        when(offertaDao.getOffertaById(9999)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(9999, "Accettata"), "client1")
        );

        assertEquals(ErrorCode.OFFERTA_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("DAO throws unexpected exception - SHOULD throw InternalServerErrorException")
    @WithMockUser(username = "client1", roles = "Cliente")
    void testDAOThrowsException_ShouldBeWrappedInGenericException() {
        when(offertaDao.getOffertaById(1)).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(
                InternalServerErrorException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(1, "Accettata"), "client1")
        );
    }

    @Test
    @DisplayName("Ownership check BEFORE state validation - SHOULD throw UnauthorizedException first")
    void testOwnershipCheckBeforeStateValidation() {
        mockJwtUser("client1", "Cliente");
        Immobile immobile = buildTestImmobile(5, "Via Torino", "agente1");
        Offerta offertaClient2Accettata = new Offerta(5, 100000.0, StatoOfferta.ACCETTATA, "client2-uid", immobile);

        when(offertaDao.getOffertaById(5)).thenReturn(offertaClient2Accettata);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> offertaService.aggiornaStatoOfferta(new AggiornaOffertaRequest(5, "Rifiutata"), "client1")
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }
}