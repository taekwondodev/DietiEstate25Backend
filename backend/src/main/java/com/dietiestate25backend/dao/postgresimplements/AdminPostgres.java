package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class AdminPostgres implements UtenteAgenziaDao {
    private final JdbcTemplate jdbcTemplate;

    public AdminPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean save(UtenteAgenzia utente) {
        return false;
    }

    @Override
    public int getIdAgenzia(String uuid) {
        String sql = "SELECT u.role, ua.idAgenzia FROM utenti u " +
                "LEFT JOIN utenteagenzia ua ON u.sub = ua.uid " +
                "WHERE u.sub = ?";

        try {
            AdminInfo adminInfo = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new AdminInfo(
                            rs.getString("role"),
                            rs.getObject("idAgenzia", Integer.class)
                    ), uuid);

            // Verifica che sia Admin (null-safe con Objects.equals)
            if (!Objects.equals(adminInfo.role(), "Admin")) {
                throw new UnauthorizedException("L'utente non è un Admin");
            }

            // Verifica che l'Admin sia associato a un'agenzia
            if (adminInfo.idAgenzia() == null) {
                throw new NotFoundException("Admin non associato a nessuna agenzia");
            }

            return adminInfo.idAgenzia();

        } catch (NullPointerException | org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Admin non trovato");
        }

    }

    private record AdminInfo(String role, Integer idAgenzia) {}
}
