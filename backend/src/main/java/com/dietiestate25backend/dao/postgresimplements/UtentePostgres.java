package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.model.Utente;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UtentePostgres implements UtenteDao {
    private final JdbcTemplate jdbcTemplate;

    public UtentePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean save(Utente u) {
        String sql = "INSERT INTO utenti (sub, email, password, role) VALUES (?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, u.getUid(), u.getEmail(), u.getPassword(), u.getRole());
        return result > 0;
    }

    @Override
    public Utente findByEmail(String email) {
        String sql = "SELECT sub, email, password, role FROM utenti WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, utenteRowMapper(), email);
    }

    @Override
    public String findEmailByUid(String uid) {
        String sql = "SELECT email FROM utenti WHERE sub = ?";
        return jdbcTemplate.queryForObject(sql, String.class, uid);
    }

    private RowMapper<Utente> utenteRowMapper() {
        return (rs, rowNum) -> new Utente(
                rs.getString("sub"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role")
        );
    }
}
