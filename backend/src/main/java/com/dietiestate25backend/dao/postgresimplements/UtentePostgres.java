package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteDao;
import com.dietiestate25backend.model.Utente;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class UtentePostgres implements UtenteDao {
    private final JdbcTemplate jdbcTemplate;

    public UtentePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean save(Utente u) {
        String sql = "INSERT INTO utenti (uid, email, password, role) VALUES (?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, u.getUid(), u.getEmail(), u.getPassword(), u.getRole());
        return result > 0;
    }

    @Override
    public void updateLoginAttempts(Utente u) {
        String sql = "UPDATE utenti SET failed_login_attempts = ?, locked_until = ? WHERE uid = ?";

        Timestamp lockedUntilTimestamp = u.getLockedUntil() != null ? Timestamp.from(u.getLockedUntil()) : null;
        jdbcTemplate.update(sql, u.getFailedLoginAttempts(), lockedUntilTimestamp, u.getUid());
    }

    @Override
    public Utente findByEmail(String email) {
        String sql = "SELECT uid, email, password, role FROM utenti WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, utenteRowMapper(), email);
    }

    @Override
    public String findEmailByUid(String uid) {
        String sql = "SELECT email FROM utenti WHERE uid = ?";
        return jdbcTemplate.queryForObject(sql, String.class, uid);
    }

    private RowMapper<Utente> utenteRowMapper() {
        return (rs, rowNum) -> {
            Utente u = new Utente(
                    rs.getString("uid"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
            );

            u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
            Timestamp lockedUntilTimestamp = rs.getTimestamp("locked_until");
            if (lockedUntilTimestamp != null) {
                u.setLockedUntil(lockedUntilTimestamp.toInstant());
            }

            return u;
        };
    }

}
