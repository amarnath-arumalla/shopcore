package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void generatesIdWhenNoneSentAndExposesItInsideTheRequest() throws Exception {
        var request = new MockHttpServletRequest("GET", "/products");
        var response = new MockHttpServletResponse();
        AtomicReference<String> seenInside = new AtomicReference<>();
        FilterChain chain = (req, res) -> seenInside.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        String header = response.getHeader(RequestIdFilter.HEADER);
        assertThat(header).isNotBlank();
        assertThat(seenInside.get()).isEqualTo(header);              // logs inside saw the same ID
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();       // cleaned up afterwards
    }

    @Test
    void reusesSafeIncomingId() throws Exception {
        var request = new MockHttpServletRequest("GET", "/products");
        request.addHeader(RequestIdFilter.HEADER, "test-123");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("test-123");
    }

    @Test
    void replacesDangerousIncomingId() throws Exception {
        var request = new MockHttpServletRequest("GET", "/products");
        request.addHeader(RequestIdFilter.HEADER, "abc\nFAKE LOG LINE");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertThat(response.getHeader(RequestIdFilter.HEADER)).doesNotContain("\n", "FAKE");
    }
}
