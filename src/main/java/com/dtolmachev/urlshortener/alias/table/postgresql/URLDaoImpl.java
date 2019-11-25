package com.dtolmachev.urlshortener.alias.table.postgresql;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.exception.NoSuchAliasException;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class URLDaoImpl implements URLDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public URLDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final String SAVE_QUERY =
            "INSERT INTO urls (id, url, create_time, expire_time) " +
            "VALUES (?, ?, ?, ?) ";

    @Override
    public boolean save(ShortenedUrl shortenedUrl) {
        final int count =
                jdbcTemplate.update(SAVE_QUERY,
                        shortenedUrl.getAlias().getValue(),
                        shortenedUrl.getUrl().toString(),
                        new Timestamp(shortenedUrl.getCreateDate().getMillis()),
                        new Timestamp(shortenedUrl.getExpireDate().getMillis()));
        return count > 0;
    }

    private static final String GET_BY_ID_QUERY =
            "SELECT id, url, create_time, expire_time " +
            "FROM urls " +
            "WHERE id = ? ";

    @Override
    public ShortenedUrl get(Alias alias) {
        String id = alias.getValue();
        ShortenedUrl result =
                jdbcTemplate.query(GET_BY_ID_QUERY,
                        this::mapShortenedUrlRow,
                        id).stream().findAny().orElse(null);
        if(result == null){
            throw new NoSuchAliasException(alias);
        }
        return result;
    }


    private static final String GET_BY_URL_QUERY =
            "SELECT id, url, create_time, expire_time " +
                    "FROM urls " +
                    "WHERE url = ? ";

    public ShortenedUrl get(URL url) {
        String urlStr = url.toString();
        return jdbcTemplate.query(GET_BY_URL_QUERY,
                        this::mapShortenedUrlRow,
                        urlStr).stream().findAny().orElse(null);
    }

    private static final String GET_ALREADY_USED_QUERY =
            "SELECT id " +
            "FROM urls " +
            "WHERE id IN (:ids) ";

    @Override
    public List<Alias> getAlreadyUsed(List<Alias> aliases) {
        List<String> aliasParam = aliases
           .stream()
           .map(Alias::getValue)
           .collect(Collectors.toList());
        Map<String, Object> params = Collections.singletonMap("ids", aliasParam);
        return namedParameterJdbcTemplate.query(GET_ALREADY_USED_QUERY, params, this::mapAliasRow);
    }

    private static final String DELETE_QUERY = "DELETE FROM urls";

    @Override
    public void clear() {
        jdbcTemplate.update(DELETE_QUERY);
    }

    private static final String SIZE_QUERY =
            "SELECT count(id) " +
            "FROM urls ";

    @Override
    public long size() {
        return jdbcTemplate.queryForObject(SIZE_QUERY, Long.class);
    }


    private static final String REMOVE_EXPIRED_QUERY =
            "DELETE  " +
            "FROM urls " +
            "WHERE expire_time < NOW() ";

    @Override
    public void cleanExpired() {
        jdbcTemplate.update(REMOVE_EXPIRED_QUERY);
    }

    @Nonnull
    @SneakyThrows
    private ShortenedUrl mapShortenedUrlRow(@Nonnull ResultSet rs, int i) throws SQLException {
        return ShortenedUrl.builder()
                .alias(Alias.create(rs.getString("id")))
                .url(new URL(rs.getString("url")))
                .createDate(new DateTime(rs.getTimestamp("create_time").getTime()))
                .expireDate(new DateTime(rs.getTimestamp("expire_time").getTime()))
                .build();
    }

    private Alias mapAliasRow(@Nonnull ResultSet rs, int i) throws SQLException {
        return Alias.create(rs.getString("id"));
    }
}
