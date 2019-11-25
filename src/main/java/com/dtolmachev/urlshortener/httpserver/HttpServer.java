package com.dtolmachev.urlshortener.httpserver;

import com.dtolmachev.urlshortener.httpserver.filters.ExceptionFilter;
import com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter;
import com.dtolmachev.urlshortener.httpserver.servlets.CreateServlet;
import com.dtolmachev.urlshortener.httpserver.servlets.GetServlet;
import com.dtolmachev.urlshortener.httpserver.servlets.HealthServlet;
import com.dtolmachev.urlshortener.httpserver.servlets.PingServlet;
import com.dtolmachev.urlshortener.httpserver.servlets.WrongMethodServlet;
import com.dtolmachev.urlshortener.httpserver.servlets.UnknownServlet;
import com.dtolmachev.urlshortener.service.DependencyGraph;
import com.dtolmachev.urlshortener.service.config.HttpServerConfig;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.concurrent.ArrayBlockingQueue;

import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.CREATE_SERVLET;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.GET_SERVLET;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.HEALTH_SERVLET;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.PING_SERVLET;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.UNKNOWN_SERVLET;
import static com.dtolmachev.urlshortener.httpserver.filters.RewriteFilter.WRONG_METHOD_SERVLET;
import static org.eclipse.jetty.servlet.ServletContextHandler.GZIP;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class HttpServer {

    private static final int ACCEPTORS_COUNT = 1;
    private static final int SELECTORS_COUNT = 1;

    private final Server server;

    private final HttpServerConfig config;

    private final DependencyGraph dependencies;

    public HttpServer(HttpServerConfig config,
                      DependencyGraph dependencies) {
        validate(config);
        this.config = config;

        this.dependencies = dependencies;
        this.server = createServer();
    }

    private void validate(HttpServerConfig config) {
        if (config.getPort() <= 0 || config.getPort() >= 65536) {
            throw new IllegalArgumentException("Invalid port, port should be in range [1, 65535]");
        }
        if (config.getQueueCapacity() < 1) {
            throw new IllegalArgumentException("Invalid queue capacity, capacity should be a positive number");
        }
        if (config.getMinThreads() < 1) {
            throw new IllegalArgumentException("Invalid min threads number, min threads should be a positive number");
        }
        if (config.getMaxThreads() < config.getMinThreads()) {
            throw new IllegalArgumentException("Invalid max threads number, max threads should be more than min threads");
        }
    }

    private Server createServer() {
        Server server = new Server(createPool());
        server.setStopAtShutdown(true);
        server.setConnectors(new Connector[]{createServerConnector(server)});
        server.setHandler(createHandler());
        return server;
    }

    private QueuedThreadPool createPool() {
        QueuedThreadPool pool = new QueuedThreadPool(
                config.getMaxThreads(),
                config.getMinThreads(),
                (int) config.getIdleTimeout().toMillis(),
                new ArrayBlockingQueue<>(config.getQueueCapacity())
        );
        pool.setName("jetty");
        return pool;
    }

    private Connector createServerConnector(Server server) {
        ServerConnector connector = new ServerConnector(
                server,
                null,
                null,
                null,
                ACCEPTORS_COUNT,
                SELECTORS_COUNT
        );
        connector.addConnectionFactory(new HttpConnectionFactory());
        connector.setPort(config.getPort());
        connector.setHost(config.getHost());
        connector.setIdleTimeout(config.getIdleTimeout().toMillis());
        return connector;
    }

    private Handler createHandler() {
        ServletContextHandler handler = new ServletContextHandler(
                null,
                "/",
                null,
                null,
                null,
                new ErrorPageErrorHandler(),
                GZIP | NO_SECURITY | NO_SESSIONS
        );
        registerFilters(handler);
        registerServlets(handler);
        return handler;
    }

    private void registerServlets(ServletContextHandler handler) {
        handler.getServletContext()
                .addServlet("create", new CreateServlet(
                        dependencies.getAliasService(),
                        dependencies.getUrlDao()))
                .addMapping("/" + CREATE_SERVLET);
        handler.getServletContext()
                .addServlet("get", new GetServlet(
                        dependencies.getUrlDao()))
                .addMapping("/" + GET_SERVLET);
        handler.addServlet(PingServlet.class, "/" + PING_SERVLET);
        handler.addServlet(HealthServlet.class, "/" + HEALTH_SERVLET);
        handler.addServlet(WrongMethodServlet.class, "/" + WRONG_METHOD_SERVLET);
        handler.addServlet(UnknownServlet.class, "/" + UNKNOWN_SERVLET);
    }

    private void registerFilters(ServletContextHandler handler) {
        handler.addFilter(ExceptionFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(RewriteFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST));
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Exception on starting http server: " + e.getMessage(), e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception on stopping http server: " + e.getMessage(), e);
        }
    }
}
