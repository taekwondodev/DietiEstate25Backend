package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

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
        String sql = "SELECT idAgenzia FROM utenteagenzia WHERE uid = ?";
        UUID uid = UUID.fromString(uuid);

        Integer idAgenzia = jdbcTemplate.queryForObject(sql, new Object[]{uid}, Integer.class);
        if (idAgenzia == null) {
            throw new NotFoundException("Admin non trovato");
        }

        return idAgenzia;
    }
}
