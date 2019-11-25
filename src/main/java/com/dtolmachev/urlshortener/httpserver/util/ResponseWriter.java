package com.dtolmachev.urlshortener.httpserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseWriter {
    private static ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.CLOSE_CLOSEABLE, true)
            .configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);

    public static void write(ServletResponse response, Object data) throws IOException {
            write(response, mapper.writeValueAsString(data).getBytes(Charsets.UTF_8));
    }

    public static void write(ServletResponse response, byte[] content) throws IOException {
        OutputStream stream = response.getOutputStream();
        response.setContentLength(content.length);
        stream.write(content);
        stream.flush();
        stream.close();
    }

}
