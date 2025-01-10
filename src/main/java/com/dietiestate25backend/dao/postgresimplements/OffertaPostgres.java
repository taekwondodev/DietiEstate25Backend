package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Offerta;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OffertaPostgres implements OffertaDao {
    private final JdbcTemplate jdbcTemplate;

    public OffertaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean salvaOfferta(Offerta offerta) {
        String sql = "INSERT INTO offerta (importo, stato, idCliente, idImmobile) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                offerta.getImporto(), offerta.getStato().getStatoString(), offerta.getIdCliente().toString(), offerta.getIdImmobile()
        ) == 1;
    }
}
