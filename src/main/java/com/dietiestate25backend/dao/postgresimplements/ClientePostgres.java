package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ClientePostgres implements UtenteDao {
    private final JdbcTemplate jdbcTemplate;

    public ClientePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean save(String uid) {
        String sql = "INSERT INTO cliente (uid) VALUES (?)";
        return jdbcTemplate.update(sql, uid) == 1;
    }
}
