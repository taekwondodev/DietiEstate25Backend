package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.UtenteAgenziaDao;
import com.dietiestate25backend.error.ErrorCode;
import com.dietiestate25backend.error.exception.NotFoundException;
import com.dietiestate25backend.error.exception.UnauthorizedException;
import com.dietiestate25backend.model.UtenteAgenzia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

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
        String sql = "SELECT u.role, ua.idAgenzia FROM utenti u " +
                "LEFT JOIN utenteagenzia ua ON u.uid = ua.uid " +
                "WHERE u.uid = ?";

        try {
            UtenteAgenziaPostgres.AdminInfo adminInfo = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new UtenteAgenziaPostgres.AdminInfo(
                            rs.getString("role"),
                            rs.getObject("idAgenzia", Integer.class)
                    ), uuid);

            if (!Objects.equals(adminInfo.role(), "Admin")) {
                throw new UnauthorizedException(ErrorCode.INSUFFICIENT_PERMISSIONS);
            }

            if (adminInfo.idAgenzia() == null) {
                throw new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            return adminInfo.idAgenzia();

        } catch (NullPointerException | org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException(ErrorCode.ADMIN_NOT_FOUND);
        }

    }

    private record AdminInfo(String role, Integer idAgenzia) {}
}