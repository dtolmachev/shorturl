package com.dtolmachev.urlshortener.httpserver.filters;

import com.dtolmachev.urlshortener.httpserver.util.routing.RequestSpec;
import com.dtolmachev.urlshortener.httpserver.util.routing.Rewriter;
import com.dtolmachev.urlshortener.httpserver.util.routing.RouteSpec;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.ALIAS_PARAMETER;
import static com.dtolmachev.urlshortener.service.config.Configuration.ALIAS_REGEX;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

public class RewriteFilter implements Filter {

    public final static String HEALTH_SERVLET = "health";
    public final static String PING_SERVLET = "ping";
    public final static String CREATE_SERVLET = "create";
    public final static String GET_SERVLET = "get";

    public static final String UNKNOWN_SERVLET = "unknown";
    public static final String WRONG_METHOD_SERVLET = "wrongmethod";

    public static final RouteSpec healthRoute = RouteSpec.builder()
            .pattern(compile("^/" + HEALTH_SERVLET + "[/]?$", CASE_INSENSITIVE))
            .method("GET")
            .converter((request, matcher) -> "/" + HEALTH_SERVLET)
            .build();

    public static final RouteSpec pingRoute = RouteSpec.builder()
            .pattern(compile("^/" + PING_SERVLET + "[/]?$", CASE_INSENSITIVE))
            .method("GET")
            .converter((request, matcher) -> "/" + PING_SERVLET)
            .build();

    public static final RouteSpec createRoute = RouteSpec.builder()
            .pattern(compile("^/" + CREATE_SERVLET + "[/]?$"))
            .method("PUT")
            .converter((request, matcher) -> "/" + CREATE_SERVLET)
            .build();

    public static final RouteSpec getRoute = RouteSpec.builder()
            .pattern(compile("^/(" + ALIAS_REGEX + ")[/]?$"))
            .method("GET")
            .converter((request, matcher) -> "/" + GET_SERVLET + "?" + ALIAS_PARAMETER + "=" + matcher.group(1))
            .build();

    public static final RouteSpec unknownRoute = RouteSpec.builder()
            .pattern(compile("^.*$", CASE_INSENSITIVE))
            .converter((request, matcher) -> "/" + UNKNOWN_SERVLET + "?uri=" + request.getUri())
            .build();

    public static final RouteSpec wrongMethodRoute = RouteSpec.builder()
            .pattern(compile("^.*$", CASE_INSENSITIVE))
            .converter((request, matcher) -> "/" + WRONG_METHOD_SERVLET + "?uri=" + request.getUri())
            .build();

    private final Rewriter rewriter;

    public RewriteFilter() {
        this.rewriter = new Rewriter(
            asList(healthRoute,
                   pingRoute,
                   createRoute,
                   getRoute,
                   unknownRoute,
                   wrongMethodRoute),
            unknownRoute,
            wrongMethodRoute
        );
    }

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request);
        wrapper.getRequestDispatcher(rewriter.apply(convert(wrapper))).forward(request, response);
    }

    private RequestSpec convert(HttpServletRequestWrapper wrapper) {
        return new RequestSpec(wrapper.getMethod(), wrapper.getRequestURI());
    }
}
