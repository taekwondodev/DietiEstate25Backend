package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.model.Immobile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ImmobilePostgres implements ImmobileDao {
    private static final String IDIMMOBILE = "idImmobile";
    private static final String URLFOTO = "urlFoto";
    private static final String DESCRIZIONE = "descrizione";
    private static final String PREZZO = "prezzo";
    private static final String DIMENSIONE = "dimensione";
    private static final String N_BAGNI = "nBagni";
    private static final String N_STANZE = "nStanze";
    private static final String TIPOLOGIA = "tipologia";
    private static final String LATITUDINE = "latitudine";
    private static final String LONGITUDINE = "longitudine";
    private static final String INDIRIZZO = "indirizzo";
    private static final String COMUNE = "comune";
    private static final String PIANO = "piano";
    private static final String ID_AGENTE = "idAgente";
    private static final String HAS_ASCENSORE = "hasAscensore";
    private static final String HAS_BALCONE = "hasBalcone";
    private static final String PREZZO_MIN = "prezzoMin";
    private static final String PREZZO_MAX = "prezzoMax";

    private final JdbcTemplate jdbcTemplate;

    public ImmobilePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Immobile> cercaImmobiliConFiltri(Map<String, Object> filters) {
       String sql = buildSql(filters);
       List<Object> parameters = buildParameters(filters);

        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            return ps;
        }, new ImmobileRowMapper());
    }

    @Override
    public boolean creaImmobile(Immobile immobile) {
        String sql = "INSERT INTO " +
                "immobile (urlFoto, descrizione, prezzo, dimensione, nBagni, nStanze, tipologia, latitudine, longitudine, indirizzo, comune, piano, hasAscensore, hasBalcone, idAgente)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int result = jdbcTemplate.update(sql,
                immobile.getUrlFoto(),
                immobile.getDescrizione(),
                immobile.getPrezzo(),
                immobile.getDimensione(),
                immobile.getnBagni(),
                immobile.getnStanze(),
                immobile.getTipologia(),
                immobile.getLatitudine(),
                immobile.getLongitudine(),
                immobile.getIndirizzo(),
                immobile.getComune(),
                immobile.getPiano(),
                immobile.isHasAscensore(),
                immobile.isHasBalcone(),
                immobile.getIdResponsabile().toString()
        );

        return result > 0;
    }

    private String buildSql(Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder("SELECT * FROM immobile WHERE 1=1");
        sql.append(" AND comune = ?");

        if (filters.containsKey(TIPOLOGIA)) {
            sql.append(" AND tipologia = ?");
        }

        if (filters.containsKey(PREZZO_MIN) && filters.containsKey(PREZZO_MAX)) {
            sql.append(" AND prezzo BETWEEN ? AND ?");
        }

        if (filters.containsKey(DIMENSIONE)) {
            sql.append(" AND dimensione = ?");
        }

        if (filters.containsKey(N_BAGNI)) {
            sql.append(" AND nBagni = ?");
        }

        return sql.toString();
    }

    private List<Object> buildParameters(Map<String, Object> filters){
        List<Object> params = new ArrayList<>();
        params.add(filters.get(COMUNE));

        if (filters.containsKey(TIPOLOGIA)) {
            params.add(filters.get(TIPOLOGIA));
        }

        if (filters.containsKey(PREZZO_MIN) && filters.containsKey(PREZZO_MAX)) {
            params.add(filters.get(PREZZO_MIN));
            params.add(filters.get(PREZZO_MAX));
        }

        if (filters.containsKey(DIMENSIONE)) {
            params.add(filters.get(DIMENSIONE));
        }

        if (filters.containsKey(N_BAGNI)) {
            params.add(filters.get(N_BAGNI));
        }

        return params;
    }

    private static class ImmobileRowMapper implements RowMapper<Immobile> {
        @Override
        public Immobile mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Immobile.Builder()
                    .setIdImmobile(rs.getInt(IDIMMOBILE))
                    .setUrlFoto(rs.getString(URLFOTO))
                    .setDescrizione(rs.getString(DESCRIZIONE))
                    .setPrezzo(rs.getDouble(PREZZO))
                    .setDimensione(rs.getInt(DIMENSIONE))
                    .setNBagni(rs.getInt(N_BAGNI))
                    .setNStanze(rs.getInt(N_STANZE))
                    .setTipologia(rs.getString(TIPOLOGIA))
                    .setLatitudine(rs.getDouble(LATITUDINE))
                    .setLongitudine(rs.getDouble(LONGITUDINE))
                    .setIndirizzo(rs.getString(INDIRIZZO))
                    .setComune(rs.getString(COMUNE))
                    .setPiano(rs.getInt(PIANO))
                    .setHasAscensore(rs.getBoolean(HAS_ASCENSORE))
                    .setHasBalcone(rs.getBoolean(HAS_BALCONE))
                    .setIdResponsabile(UUID.fromString(rs.getString(ID_AGENTE)))
                    .build();
        }
    }
}
