package com.dtolmachev.urlshortener.httpserver.servlets;

import com.dtolmachev.urlshortener.alias.model.Alias;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.exception.NoSuchAliasException;
import com.dtolmachev.urlshortener.httpserver.util.ParametersUtil;
import com.dtolmachev.urlshortener.httpserver.util.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.LOCATION_HEADER;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;

public class GetServlet extends HttpServlet {

    private final URLDao urlDao;

    public GetServlet(URLDao urlDao) {
        this.urlDao = urlDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Alias alias = Alias.create(req.getParameter(ParametersUtil.ALIAS_PARAMETER));

        ShortenedUrl shortenedUrl = urlDao.get(alias);
        URL url = shortenedUrl.getUrl();
        resp.setHeader(LOCATION_HEADER, url.toString());
        resp.setStatus(SC_MOVED_PERMANENTLY);
    }
}
