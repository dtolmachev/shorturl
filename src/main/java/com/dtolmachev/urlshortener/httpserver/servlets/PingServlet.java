package com.dtolmachev.urlshortener.httpserver.servlets;

import com.dtolmachev.urlshortener.httpserver.util.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;

public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        ResponseWriter.write(resp, "pong");
        resp.setStatus(SC_OK);
    }
}
