package com.shopcore.api;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Gives every request a ticket number, stamps it on every log line, and returns it to the client. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)          // run first, so even early logs carry the ID
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    // accept a caller's ID only if it's harmless (stops log injection of newlines/HTML)
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String requestId = (incoming != null && SAFE_ID.matcher(incoming).matches())
                ? incoming
                : UUID.randomUUID().toString();

        MDC.put(MDC_KEY, requestId);              // every log line on this thread now includes it
        response.setHeader(HEADER, requestId);
        try {
            chain.doFilter(request, response);    // controller, service, repository run inside here
        } finally {
            MDC.remove(MDC_KEY);                  // threads are REUSED by Tomcat: always clean up
        }
    }
}