package com.dietiestate25backend.dao.postgresImplements;

import com.dietiestate25backend.dao.modelInterface.ImmobileDao;
import com.dietiestate25backend.model.Immobile;
import com.dietiestate25backend.model.Indirizzo;
import com.dietiestate25backend.model.TipoClasseEnergetica;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ImmobilePostgres implements ImmobileDao {
    private final JdbcTemplate jdbcTemplate;

    public ImmobilePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Immobile> cercaImmobiliConFiltri(Map<String, Object> filters) {
       String sql = buildSql(filters);
       List<Object> parameters = buildParameters(filters);

       return jdbcTemplate.query(sql.toString(), parameters.toArray(), (rs, rowNum) -> {
           double prezzo = rs.getDouble("prezzo");
           String nStanze = rs.getString("nStanze");
           String tipologia = rs.getString("tipologia");
           Indirizzo indirizzo = new Indirizzo(rs.getString("via"), rs.getString("civico"), rs.getString("cap"));
           TipoClasseEnergetica classeEnergetica = TipoClasseEnergetica.fromString(rs.getString("classeEnergetica"));

           return new Immobile(prezzo, nStanze, tipologia, indirizzo, classeEnergetica);
       });
    }

    private String buildSql(Map<String, Object> filters){
        StringBuilder sql = new StringBuilder("SELECT * FROM immobile WHERE 1=1");
        sql.append(" AND cap = ?");

        if (filters.containsKey("prezzoMin") && filters.containsKey("prezzoMax")) {
            sql.append(" AND prezzo BETWEEN ? AND ?");
        }

        if (filters.containsKey("nStanze")) {
            sql.append(" AND nStanze = ?");
        }

        if (filters.containsKey("tipologia")) {
            sql.append(" AND tipologia = ?");
        }

        if (filters.containsKey("classeEnergetica")) {
            sql.append(" AND classeEnergetica = ?");
        }

        return sql.toString();
    }

    private List<Object> buildParameters(Map<String, Object> filters){
        List<Object> params = new ArrayList<>();
        params.add(filters.get("cap"));

        if (filters.containsKey("prezzoMin") && filters.containsKey("prezzoMax")) {
            params.add(filters.get("prezzoMin"));
            params.add(filters.get("prezzoMax"));
        }

        if (filters.containsKey("nStanze")) {
            params.add(filters.get("nStanze"));
        }

        if (filters.containsKey("tipologia")) {
            params.add(filters.get("tipologia"));
        }

        if (filters.containsKey("classeEnergetica")) {
            params.add(((TipoClasseEnergetica) filters.get("classeEnergetica")).getClasse());
        }

        return params;
    }
}
