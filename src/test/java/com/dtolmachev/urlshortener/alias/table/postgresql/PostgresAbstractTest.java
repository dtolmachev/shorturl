package com.dtolmachev.urlshortener.alias.table.postgresql;

import com.dtolmachev.urlshortener.service.config.DataSourceConfig;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.dtolmachev.urlshortener.service.DependencyGraphImpl.createDataSource;

public abstract class PostgresAbstractTest {

    protected JdbcTemplate jdbcTemplate;
    protected PGPoolingDataSource dataSource;

    public static final DataSourceConfig testDataSourceConfig = DataSourceConfig.builder()
            .name("simple_test_datasource")
            .host("127.0.0.1")
            .port(5432)
            .dbName("shorturl_test")
            .username("shorturl_test")
            .password("shorturl_test")
            .maxConnections(10)
            .build();

    protected PostgresAbstractTest() {
        this.dataSource = createDataSource(testDataSourceConfig);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
