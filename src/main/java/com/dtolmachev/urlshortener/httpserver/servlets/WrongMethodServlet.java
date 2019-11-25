package com.dtolmachev.urlshortener.httpserver.servlets;

import com.dtolmachev.urlshortener.httpserver.util.ResponseWriter;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.eclipse.jetty.http.HttpMethod.DELETE;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpMethod.HEAD;
import static org.eclipse.jetty.http.HttpMethod.OPTIONS;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.eclipse.jetty.http.HttpMethod.PUT;
import static org.eclipse.jetty.http.HttpMethod.TRACE;

public class WrongMethodServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(GET, req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(HEAD, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(POST, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(PUT, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(DELETE, req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(OPTIONS, req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(TRACE, req, resp);
    }

    private void processRequest(HttpMethod method,
                                HttpServletRequest req,
                                HttpServletResponse resp) throws IOException {
        String originalURI = ((Request) req).getOriginalURI();
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        ResponseWriter.write(resp, String.format("%s method is not supported by %s", method, originalURI));
    }
}
