package com.dietiestate25backend.dao.postgresimplements;

import com.dietiestate25backend.dao.modelinterface.ImmobileDao;
import com.dietiestate25backend.model.Immobile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ImmobilePostgres implements ImmobileDao {
    private static final String DESCRIZIONE = "descrizione";
    private static final String PREZZO = "prezzo";
    private static final String N_BAGNI = "nBagni";
    private static final String N_STANZE = "nStanze";
    private static final String TIPOLOGIA = "tipologia";
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

       return jdbcTemplate.query(sql, (rs, rowNum) -> {
           String descrizione = rs.getString(DESCRIZIONE);
           double prezzo = rs.getDouble(PREZZO);
           int nBagni = rs.getInt(N_BAGNI);
           int nStanze = rs.getInt(N_STANZE);
           String tipologia = rs.getString(TIPOLOGIA);
           Indirizzo indirizzo = new Indirizzo(rs.getString(VIA), rs.getString(CIVICO), rs.getString(CAP));
           TipoClasseEnergetica classeEnergetica = TipoClasseEnergetica.fromString(rs.getString(CLASSE_ENERGETICA));
           int piano = rs.getInt(PIANO);
           boolean hasAscensore = rs.getBoolean(HAS_ASCENSORE);
           boolean hasBalcone = rs.getBoolean(HAS_BALCONE);
           UUID idResponsabile = UUID.fromString(rs.getString(ID_AGENTE));

           return new Immobile(descrizione, prezzo, nBagni, nStanze, tipologia, indirizzo, classeEnergetica, piano, hasAscensore, hasBalcone, idResponsabile);

       }, parameters.toArray());
    }

    @Override
    public boolean creaImmobile(Immobile immobile) {
        String sql = "INSERT INTO " +
                "immobile (urlFoto, descrizione, prezzo, nBagni, nStanze, tipologia, coordinate, indirizzo, piano, hasAscensore, hasBalcone, idAgente)" +
                " VALUES (?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, ?, ?, ?)";

        int result = jdbcTemplate.update(sql,
                immobile.getUrlFoto(),
                immobile.getDescrizione(),
                immobile.getPrezzo(),
                immobile.getnBagni(),
                immobile.getnStanze(),
                immobile.getTipologia(),
                immobile.getLongitudine(),
                immobile.getLatitudine(),
                immobile.getIndirizzo(),
                immobile.getPiano(),
                immobile.isHasAscensore(),
                immobile.isHasBalcone(),
                immobile.getIdResponsabile().toString()
        );

        return result > 0;
    }

    private String buildSql(Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder("SELECT * FROM immobile WHERE 1=1");
        sql.append(" AND cap = ?");

        if (filters.containsKey(PREZZO_MIN) && filters.containsKey(PREZZO_MAX)) {
            sql.append(" AND prezzo BETWEEN ? AND ?");
        }

        if (filters.containsKey(N_STANZE)) {
            sql.append(" AND nStanze = ?");
        }

        if (filters.containsKey(TIPOLOGIA)) {
            sql.append(" AND tipologia = ?");
        }

        if (filters.containsKey(CLASSE_ENERGETICA)) {
            sql.append(" AND classeEnergetica = ?");
        }

        return sql.toString();
    }

    private List<Object> buildParameters(Map<String, Object> filters){
        List<Object> params = new ArrayList<>();
        params.add(filters.get(CAP));

        if (filters.containsKey(PREZZO_MIN) && filters.containsKey(PREZZO_MAX)) {
            params.add(filters.get(PREZZO_MIN));
            params.add(filters.get(PREZZO_MAX));
        }

        if (filters.containsKey(N_STANZE)) {
            params.add(filters.get(N_STANZE));
        }

        if (filters.containsKey(TIPOLOGIA)) {
            params.add(filters.get(TIPOLOGIA));
        }

        if (filters.containsKey(CLASSE_ENERGETICA)) {
            params.add(((TipoClasseEnergetica) filters.get(CLASSE_ENERGETICA)).getClasse());
        }

        return params;
    }
}
