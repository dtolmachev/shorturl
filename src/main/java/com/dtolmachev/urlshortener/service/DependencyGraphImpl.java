package com.dtolmachev.urlshortener.service;

import com.dtolmachev.urlshortener.alias.AliasServiceImpl;
import com.dtolmachev.urlshortener.alias.URLCleaner;
import com.dtolmachev.urlshortener.alias.generator.AliasGenerator;
import com.dtolmachev.urlshortener.alias.generator.AliasGeneratorImpl;
import com.dtolmachev.urlshortener.alias.table.AliasDao;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.postgresql.AliasDaoImpl;
import com.dtolmachev.urlshortener.alias.table.postgresql.URLDaoImpl;
import com.dtolmachev.urlshortener.service.config.Configuration;
import com.dtolmachev.urlshortener.service.config.DataSourceConfig;
import com.dtolmachev.urlshortener.service.config.TransactionTemplateConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Value
@AllArgsConstructor
public class DependencyGraphImpl implements DependencyGraph {

    private final PGPoolingDataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private final AliasDao aliasDao;
    private final AliasGenerator aliasGenerator;
    private final AliasServiceImpl aliasService;
    private final URLDao urlDao;

    private final URLCleaner urlCleaner;

    public static DependencyGraph create() {
        PGPoolingDataSource dataSource = createDataSource(Configuration.dataSourceConfig);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        URLDao urlDao = new URLDaoImpl(jdbcTemplate);
        AliasDao aliasDao = new AliasDaoImpl(jdbcTemplate);

        AliasGenerator aliasGenerator = new AliasGeneratorImpl();
        AliasServiceImpl aliasService = new AliasServiceImpl(
                Configuration.aliasServiceConfig,
                aliasDao,
                urlDao,
                aliasGenerator);

        URLCleaner urlCleaner = new URLCleaner(Configuration.urlCleanerConfig,
                urlDao);

        return new DependencyGraphImpl(dataSource,
                jdbcTemplate,
                aliasDao,
                aliasGenerator,
                aliasService,
                urlDao,
                urlCleaner);
    }

    public static PGPoolingDataSource createDataSource(DataSourceConfig config) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setDataSourceName("simple_datasource");
        dataSource.setServerName(config.getHost());
        dataSource.setPortNumber(config.getPort());
        dataSource.setDatabaseName(config.getDbName());
        dataSource.setUser(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setMaxConnections(config.getMaxConnections());
        return dataSource;
    }

    private static TransactionTemplate createTransactionManager(TransactionTemplateConfig config,
                                                                DataSourceTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        transactionTemplate.setTimeout(config.getTimeout());
        transactionTemplate.setIsolationLevel(config.getIsolationLevel());
        return transactionTemplate;
    }

    @Override
    public void start() {
        aliasService.start();
        urlCleaner.start();
    }

    @Override
    @SneakyThrows
    public void stop() {
        aliasService.stop();
        urlCleaner.stop();
        dataSource.close();
    }
}
