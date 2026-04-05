package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.VisitaDao;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.StatoVisita;
import com.dietiestate25backend.model.Visita;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.UUID;

@Repository
public class VisitaPostgres implements VisitaDao {
    private final JdbcTemplate jdbcTemplate;

    public VisitaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean salva(Date data, Time time, StatoVisita stato, String uidCliente, int idImmobile) {
        String sql = "INSERT INTO visita (data, orario, stato, idCliente, idImmobile) VALUES (?, ?, CAST(? AS statovisita), ?, ?)";

        int result = jdbcTemplate.update(sql,
                data, time, stato.getStatoString(), uidCliente, idImmobile
        );
        return result > 0;
    }

    @Override
    public boolean aggiornaStato(Visita visita) {
        String sql = "UPDATE visita SET stato = CAST(? AS statovisita) WHERE idVisita = ?";

        int result = jdbcTemplate.update(sql,
                visita.getStato().getStatoString(),
                visita.getIdVisita()
        );
        return result > 0;
    }

    @Override
    public Visita getVisitaById(int id) {
        String sql = "SELECT v.*, i.* " +
                "FROM visita v " +
                "JOIN immobile i ON v.idImmobile = i.idImmobile " +
                "WHERE v.idVisita = ?";

        return jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
            Immobile immobile = buildImmobile(resultSet);

            return new Visita(
                    resultSet.getInt("idVisita"),
                    resultSet.getDate("data"),
                    resultSet.getTime("orario"),
                    StatoVisita.fromString(resultSet.getString("stato")),
                    resultSet.getString("idCliente"),
                    immobile
            );
        }, id);
    }

    @Override
    public List<Visita> riepilogoVisiteCliente(String idCliente) {
        String sql = "SELECT v.*, i.* " +
                "FROM visita v " +
                "JOIN immobile i ON v.idImmobile = i.idImmobile " +
                "WHERE v.idCliente = ?";

        return getVisitas(idCliente, sql);
    }

    @Override
    public List<Visita> riepilogoVisiteUtenteAgenzia(String idAgente) {
        String sql = "SELECT v.*, i.* " +
                "FROM visita v " +
                "JOIN immobile i ON v.idImmobile = i.idImmobile " +
                "WHERE i.idAgente = ?";

        return getVisitas(idAgente, sql);
    }

    private List<Visita> getVisitas(String idAgente, String sql) {
        return jdbcTemplate.query(sql, (resultSet, i) -> {
            Immobile immobile = buildImmobile(resultSet);

            return new Visita(
                    resultSet.getInt("idVisita"),
                    resultSet.getDate("data"),
                    resultSet.getTime("orario"),
                    StatoVisita.fromString(resultSet.getString("stato")),
                    resultSet.getString("idCliente"),
                    immobile
            );
        }, idAgente);
    }

    static Immobile buildImmobile(ResultSet resultSet) throws SQLException {
        return Immobile.builder()
                .idImmobile(resultSet.getInt("idImmobile"))
                .urlFoto(resultSet.getString("urlFoto"))
                .descrizione(resultSet.getString("descrizione"))
                .prezzo(resultSet.getDouble("prezzo"))
                .dimensione(resultSet.getInt("dimensione"))
                .nBagni(resultSet.getInt("nBagni"))
                .nStanze(resultSet.getInt("nStanze"))
                .tipologia(resultSet.getString("tipologia"))
                .latitudine(resultSet.getDouble("latitudine"))
                .longitudine(resultSet.getDouble("longitudine"))
                .indirizzo(resultSet.getString("indirizzo"))
                .comune(resultSet.getString("comune"))
                .piano(resultSet.getInt("piano"))
                .hasAscensore(resultSet.getBoolean("hasAscensore"))
                .hasBalcone(resultSet.getBoolean("hasBalcone"))
                .idResponsabile(resultSet.getString("idAgente"))
                .build();
    }
}
