package com.dtolmachev.urlshortener.httpserver.util.routing;

import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class Rewriter {

    private final List<RouteSpec> routes;
    private final RouteSpec unknownRoute;
    private final RouteSpec wrongMethodRoute;

    public Rewriter(List<RouteSpec> routes,
                    RouteSpec unknownRoute,
                    RouteSpec wrongMethodRoute) {
       this.routes = routes;
       this.unknownRoute = unknownRoute;
       this.wrongMethodRoute = wrongMethodRoute;
    }

    public String apply(RequestSpec request) {
        RouteSpec pathMatch = routes.stream()
                .filter(route -> route.pathMatch(request))
                .findFirst()
                .orElse(unknownRoute);

        return pathMatch
                .methodMatch(request)
                .orElse(wrongMethodRoute
                        .methodMatch(request)
                        .orElseThrow(() -> new IllegalStateException("Not found route!")));
    }
}
