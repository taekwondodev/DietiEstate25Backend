package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.OffertaDao;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Offerta;
import com.dietiestate25backend.model.StatoOfferta;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.dietiestate25backend.dao.postgresimplements.VisitaPostgres.buildImmobile;

@Repository
public class OffertaPostgres implements OffertaDao {
    private final JdbcTemplate jdbcTemplate;

    public OffertaPostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean salvaOfferta(Offerta offerta) {
        String sql = "INSERT INTO offerta (importo, stato, idCliente, idImmobile) VALUES (?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql,
                offerta.getImporto(),
                offerta.getStato().toString(),
                offerta.getIdCliente().toString(),
                offerta.getImmobile().getIdImmobile()
        );
        return result > 0;
    }

    @Override
    public boolean aggiornaStatoOfferta(Offerta offerta) {
        String sql = "UPDATE offerta SET stato = ? WHERE idOfferta = ?";

        int result = jdbcTemplate.update(sql,
                offerta.getStato().toString(),
                offerta.getIdOfferta()
        );
        return result > 0;
    }

    @Override
    public List<Offerta> riepilogoOfferteCliente(UUID idCliente) {
        String sql = "SELECT o.*, i.* " +
                "FROM offerta o " +
                "JOIN immobile i ON o.idImmobile = i.idImmobile " +
                "WHERE o.idCliente = ?";

        return getOffertas(idCliente, sql);
    }

    @Override
    public List<Offerta> riepilogoOfferteUteneAgenzia(UUID idAgente) {
        String sql = "SELECT o.*, i.* " +
                "FROM offerta o " +
                "JOIN immobile i ON o.idImmobile = i.idImmobile " +
                "WHERE i.idAgente = ?";

        return getOffertas(idAgente, sql);
    }

    private List<Offerta> getOffertas(UUID idAgente, String sql) {
        return jdbcTemplate.query(sql, (resultSet, i) -> {
            Immobile immobile = buildImmobile(resultSet);

            return new Offerta(
                    resultSet.getInt("idOfferta"),
                    resultSet.getDouble("importo"),
                    StatoOfferta.fromString(resultSet.getString("stato")),
                    UUID.fromString(resultSet.getString("idCliente")),
                    immobile
            );
        }, idAgente.toString());
    }

}
