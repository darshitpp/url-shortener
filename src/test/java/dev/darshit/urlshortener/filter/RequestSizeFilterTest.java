package dev.darshit.urlshortener.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestSizeFilterTest {

    private final RequestSizeFilter filter = new RequestSizeFilter();

    @Test
    void acceptsExactLimitAndRejectsOneMoreByte() throws Exception {
        MockHttpServletRequest accepted = postWithBody(new byte[RequestSizeFilter.MAX_REQUEST_BYTES]);
        MockHttpServletResponse acceptedResponse = new MockHttpServletResponse();
        filter.doFilter(accepted, acceptedResponse, new MockFilterChain());
        assertEquals(200, acceptedResponse.getStatus());

        MockHttpServletRequest rejected = postWithBody(new byte[RequestSizeFilter.MAX_REQUEST_BYTES + 1]);
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
        filter.doFilter(rejected, rejectedResponse, new MockFilterChain());
        assertEquals(413, rejectedResponse.getStatus());
    }

    @Test
    void rejectsUnknownLengthBodyOneByteOverLimit() throws Exception {
        HttpServletRequest rejected = unknownLength(
                postWithBody(new byte[RequestSizeFilter.MAX_REQUEST_BYTES + 1]));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(rejected, response, new MockFilterChain());

        assertEquals(413, response.getStatus());
    }

    @Test
    void appliesToTrailingSlashAndPathParameters() throws Exception {
        for (String path : java.util.List.of(
                "/shorten/", "/shorten;untrusted", "//shorten", "/shorten//", "/%73horten")) {
            MockHttpServletRequest rejected =
                    postWithBody(path, new byte[RequestSizeFilter.MAX_REQUEST_BYTES + 1]);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(rejected, response, new MockFilterChain());

            assertEquals(413, response.getStatus());
        }
    }

    @Test
    void replaysAcceptedUnknownLengthBody() throws Exception {
        byte[] body = "\"{\"url\":\"https://example.com\"}".getBytes(StandardCharsets.UTF_8);
        HttpServletRequest request = unknownLength(postWithBody(body));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        RequestSizeFilter.ReplayRequestWrapper wrapped =
                (RequestSizeFilter.ReplayRequestWrapper) chain.getRequest();
        assertEquals(body.length, wrapped.bodyLength);
        assertEquals(body.length, wrapped.getContentLengthLong());
        assertArrayEquals(body, readAll(wrapped.getInputStream()));
    }

    @Test
    void writesJsonOnRejection() throws Exception {
        MockHttpServletRequest rejected =
                postWithBody(new byte[RequestSizeFilter.MAX_REQUEST_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(rejected, response, new MockFilterChain());

        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString()
                .contains("\"error\":\"Request body too large\"}"));
    }

    private HttpServletRequest unknownLength(MockHttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public int getContentLength() {
                return -1;
            }

            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
    }

    private byte[] readAll(javax.servlet.ServletInputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private MockHttpServletRequest postWithBody(byte[] body) {
        return postWithBody("/shorten", body);
    }

    private MockHttpServletRequest postWithBody(String path, byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setContentType("application/json");
        request.setContent(body);
        return request;
    }
}




