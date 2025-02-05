package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
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
        String sql = "INSERT INTO utenteagenzia (uid, idAgenzia, ruolo) VALUES (?, ?, ?)";

        int result = jdbcTemplate.update(sql, utente.getUid(), utente.getIdAgenzia(), utente.getRuolo());
        return result > 0;
    }

    @Override
    public int getIdAgenzia(String uid) {
        return 0;
    }
}
