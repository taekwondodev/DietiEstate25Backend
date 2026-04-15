package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UtenteAgenziaPostgres implements UtenteAgenziaDao {
    private final JdbcTemplate jdbcTemplate;

    public UtenteAgenziaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean save(UtenteAgenzia utente) {
        String sql = "INSERT INTO utenteagenzia (uid, idAgenzia) VALUES (?, ?)";

        int result = jdbcTemplate.update(sql, utente.getUid(), utente.getIdAgenzia());
        return result > 0;
    }

    @Override
    public int getIdAgenzia(String uuid) {
        String sql = "SELECT idAgenzia FROM utenteagenzia WHERE uid = ?";

        try {
            Integer idAgenzia = jdbcTemplate.queryForObject(sql, Integer.class, uuid);
            if (idAgenzia == null) {
                throw new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            return idAgenzia;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(ErrorCode.ADMIN_NOT_FOUND);
        }
    }
}