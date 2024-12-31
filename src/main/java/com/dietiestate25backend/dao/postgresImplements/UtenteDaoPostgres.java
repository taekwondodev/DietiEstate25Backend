package com.dietiestate25backend.dao.postgresImplements;

import com.dietiestate25backend.dao.modelInterface.UtenteDaoInterface;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UtenteDaoPostgres implements UtenteDaoInterface {
    private final JdbcTemplate jdbcTemplate;

    public UtenteDaoPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean login(String email, String password) {
        return false;
    }

    @Override
    public boolean registrazione(String email, String password) {
        return false;
    }
}
