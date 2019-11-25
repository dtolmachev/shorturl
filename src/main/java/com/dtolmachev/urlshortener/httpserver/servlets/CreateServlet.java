package com.dtolmachev.urlshortener.httpserver.servlets;

import com.dtolmachev.urlshortener.alias.AliasService;
import com.dtolmachev.urlshortener.alias.model.ShortenedUrl;
import com.dtolmachev.urlshortener.alias.table.URLDao;
import com.dtolmachev.urlshortener.alias.table.exception.EmptyAliasTableException;
import com.dtolmachev.urlshortener.alias.table.exception.TooLongUrlException;
import com.dtolmachev.urlshortener.alias.table.exception.WrongUrlFormatException;
import com.dtolmachev.urlshortener.httpserver.util.ParametersUtil;
import com.dtolmachev.urlshortener.httpserver.util.ResponseWriter;
import com.dtolmachev.urlshortener.alias.AliasServiceImpl;
import com.dtolmachev.urlshortener.alias.model.Alias;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.dtolmachev.urlshortener.httpserver.util.ParametersUtil.URL_MAX_LENGTH;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class CreateServlet extends HttpServlet {

    private final AliasService aliasService;
    private final URLDao urlDao;

    public CreateServlet(AliasService aliasService, URLDao urlDao) {
        this.aliasService = aliasService;
        this.urlDao = urlDao;
    }

    @Override
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        String urlAsStr = URLDecoder.decode(req.getParameter("url"), StandardCharsets.UTF_8);

        if (urlAsStr == null) {
            throw new WrongUrlFormatException(null);
        }

        if (urlAsStr.length() >= URL_MAX_LENGTH) {
            throw new TooLongUrlException(urlAsStr);
        }

        URL url;
        try {
            url = new URL(urlAsStr);
        } catch (MalformedURLException e) {
            throw new WrongUrlFormatException(e, urlAsStr);
        }

        ShortenedUrl alreadySaved = urlDao.get(url);
        if (alreadySaved != null) {
            ResponseWriter.write(resp, alreadySaved.getAlias().getValue());
            resp.setStatus(SC_OK);
            return;
        }

        Alias newAlias;
        try {
            newAlias = aliasService.getRandomAlias();
        } catch (EmptyAliasTableException e) {
            throw new EmptyAliasTableException();
        }

        DateTime create = DateTime.now();
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
            .alias(newAlias)
            .url(url)
            .createDate(create)
            .expireDate(create.plus(ParametersUtil.DEFAULT_TTL))
            .build();
        urlDao.save(shortenedUrl);
        ResponseWriter.write(resp, newAlias.getValue());
        resp.setStatus(SC_OK);
    }
}
