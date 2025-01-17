package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.model.Visita;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VisitaPostgres implements VisitaDao {
    private final JdbcTemplate jdbcTemplate;

    public VisitaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean salva(Visita visita) {
        String sql = "INSERT INTO visita (dataRichiesta, data, orario, stato, idCliente, idImmobile) VALUES (?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                visita.getDataRichiesta(), visita.getDataVisita(), visita.getOraVisita(),
                visita.getStato().getStatoString(), visita.getIdCliente().toString(), visita.getIdImmobile()
        ) == 1;
    }

    @Override
    public boolean aggiornaStato(Visita visita) {
        String sql = "UPDATE visita SET stato = ? WHERE idCliente = ? AND idImmobile = ?";

        return jdbcTemplate.update(sql,
                visita.getStato().getStatoString(), visita.getIdCliente().toString(), visita.getIdImmobile()
        ) == 1;
    }

}
