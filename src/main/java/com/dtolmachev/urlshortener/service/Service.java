package com.dtolmachev.urlshortener.service;

import com.dtolmachev.urlshortener.service.config.Configuration;
import com.dtolmachev.urlshortener.httpserver.HttpServer;

public class Service {

    private final String name;
    private final HttpServer httpServer;
    private final DependencyGraph dependencies;

    public Service() {
        this.name = Configuration.serviceName;
        this.dependencies = DependencyGraphImpl.create();
        //this.dependencies = InMemoryDependencyGraph.create();

        this.httpServer = new HttpServer(Configuration.httpServerConfig, dependencies);
    }

    public void start() {
        dependencies.start();
        httpServer.start();
    }

    public void stop() {
        httpServer.stop();
        dependencies.stop();
    }

    public String getName() {
        return name;
    }
}
