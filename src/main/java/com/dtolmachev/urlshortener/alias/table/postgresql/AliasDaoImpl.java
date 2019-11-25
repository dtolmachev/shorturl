package com.dtolmachev.urlshortener.alias.table.postgresql;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasTableException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AliasDaoImpl implements AliasDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AliasDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final String SIZE_QUERY =
            "SELECT count(id) " +
            "FROM alias";

    @Override
    public long size() {
       return jdbcTemplate.queryForObject(SIZE_QUERY, Long.class);
    }


    private static final String GET_QUERY =
            "SELECT id " +
            "FROM alias " +
            "LIMIT 1 ";

    @Override
    public Alias get() {
        Alias alias = jdbcTemplate.query(GET_QUERY, this::mapAliasRow)
                .stream()
                .findAny()
                .orElse(null);
        if (alias == null) {
            throw new EmptyAliasTableException();
        }
        return alias;
    }


    private static final String GET_WITH_SIZE_QUERY =
            "SELECT id " +
            "FROM alias " +
            "LIMIT ? ";

    @Override
    public List<Alias> get(int size) {
        return jdbcTemplate.query(GET_WITH_SIZE_QUERY, this::mapAliasRow, size);
    }


    private static final String DELETE_BY_ID_QUERY =
            "DELETE FROM alias " +
            "WHERE id = ?";

    public void deleteById(Alias alias) {
        jdbcTemplate.update(DELETE_BY_ID_QUERY, alias.getValue());
    }

    private static final String DELETE_BY_MULTIPLE_ID_QUERY =
            "DELETE FROM alias " +
            "WHERE id IN (:ids)";

    @Override
    public boolean remove(List<Alias> aliases) {
        List<String> aliasParam = aliases
                .stream()
                .map(Alias::getValue)
                .collect(Collectors.toList());
        Map<String, Object> params = Collections.singletonMap("ids", aliasParam);
        int count = namedParameterJdbcTemplate.update(DELETE_BY_MULTIPLE_ID_QUERY, params);
        return count > 1;
    }


    private static final String PUT_QUERY =
            "INSERT INTO alias (id) " +
            "VALUES (?) " +
            "ON CONFLICT DO NOTHING";

    @Override
    public void save(List<Alias> aliases) {
        List<Object[]> params = aliases
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(PUT_QUERY, params);
    }


    private static final String DELETE_QUERY = "DELETE FROM alias";

    @Override
    public void clear() {
        jdbcTemplate.update(DELETE_QUERY);
    }

    @Nonnull
    private Alias mapAliasRow(@Nonnull ResultSet rs, int i) throws SQLException {
        return Alias.create(rs.getString("id"));
    }

    private Object[] convert(Alias alias) {
        return new Object[]{alias.getValue()};
    }
}
