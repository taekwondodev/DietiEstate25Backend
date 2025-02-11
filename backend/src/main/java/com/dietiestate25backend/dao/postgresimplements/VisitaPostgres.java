package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class VisitaPostgres implements VisitaDao {
    private final JdbcTemplate jdbcTemplate;

    public VisitaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean salva(Visita visita) {
        String sql = "INSERT INTO visita (data, orario, stato, idCliente, idImmobile) VALUES (?, ?, ?, ?, ?)";

        int result = jdbcTemplate.update(sql,
                visita.getDataVisita(), visita.getOraVisita(),
                visita.getStato().getStatoString(), visita.getIdCliente().toString(),
                visita.getImmobile().getIdImmobile()
        );
        return result > 0;
    }

    @Override
    public boolean aggiornaStato(Visita visita) {
        String sql = "UPDATE visita SET stato = ? WHERE idVisita = ?";

        int result = jdbcTemplate.update(sql,
                visita.getStato().toString(),
                visita.getIdVisita()
        );
        return result > 0;
    }

    @Override
    public List<Visita> riepilogoVisiteCliente(UUID idCliente) {
        String sql = "SELECT v.*, i.* " +
                "FROM visita v " +
                "JOIN immobile i ON v.idImmobile = i.idImmobile " +
                "WHERE v.idCliente = ?";

        return jdbcTemplate.query(sql, (resultSet, i) -> {
            Immobile immobile = new Immobile(
                    /// TODO: da fixare
            );
            return new Visita(
                    resultSet.getInt("idVisita"),
                    resultSet.getDate("data"),
                    resultSet.getTime("orario"),
                    StatoVisita.fromString(resultSet.getString("stato")),
                    UUID.fromString(resultSet.getString("idCliente")),
                    immobile
            );
        }, idCliente.toString());
    }

}
