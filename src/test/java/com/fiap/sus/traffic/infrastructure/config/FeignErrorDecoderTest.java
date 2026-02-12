package com.fiap.sus.traffic.infrastructure.config;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FeignErrorDecoderTest {

    private FeignErrorDecoder errorDecoder;
    private Request originalRequest;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();
        originalRequest = Request.create(
                Request.HttpMethod.GET,
                "http://example.com/test",
                new HashMap<>(),
                null,
                null,
                null
        );
    }

    @Test
    void deveLancarHttpClientErrorExceptionParaErro4xx() {
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpClientErrorException.class, exception);
        HttpClientErrorException httpException = (HttpClientErrorException) exception;
        assertEquals(404, httpException.getStatusCode().value());
    }

    @Test
    void deveLancarHttpServerErrorExceptionParaErro5xx() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpServerErrorException.class, exception);
        HttpServerErrorException httpException = (HttpServerErrorException) exception;
        assertEquals(500, httpException.getStatusCode().value());
    }

    @Test
    void deveLancarHttpClientErrorExceptionParaErro400() {
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpClientErrorException.class, exception);
        HttpClientErrorException httpException = (HttpClientErrorException) exception;
        assertEquals(400, httpException.getStatusCode().value());
    }

    @Test
    void deveLancarHttpClientErrorExceptionParaErro422() {
        Response response = Response.builder()
                .status(422)
                .reason("Unprocessable Entity")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpClientErrorException.class, exception);
        HttpClientErrorException httpException = (HttpClientErrorException) exception;
        assertEquals(422, httpException.getStatusCode().value());
    }

    @Test
    void deveLancarHttpServerErrorExceptionParaErro502() {
        Response response = Response.builder()
                .status(502)
                .reason("Bad Gateway")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpServerErrorException.class, exception);
        HttpServerErrorException httpException = (HttpServerErrorException) exception;
        assertEquals(502, httpException.getStatusCode().value());
    }

    @Test
    void deveLancarHttpServerErrorExceptionParaErro503() {
        Response response = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .headers(new HashMap<>())
                .request(originalRequest)
                .build();

        Exception exception = errorDecoder.decode("testMethod", response);

        assertInstanceOf(HttpServerErrorException.class, exception);
        HttpServerErrorException httpException = (HttpServerErrorException) exception;
        assertEquals(503, httpException.getStatusCode().value());
    }
}
