package de.bund.digitalservice.ris.caselaw.webtestclient;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class RisRequestSpec {
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final OidcLoginRequestPostProcessor login;
  private final Cookie csrfCookie;
  private HttpMethod httpMethod;
  private URI uri;
  private Supplier<?> bodySupplier;
  private String json;
  private byte[] bodyAsBytes;
  private MediaType mediaType;
  private MockMultipartFile file;
  private final Map<String, String> additionalHeaders;

  public RisRequestSpec(
      MockMvc mockMvc,
      ObjectMapper objectMapper,
      OidcLoginRequestPostProcessor login,
      Cookie csrfCookie) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.login = login;
    this.csrfCookie = csrfCookie;
    this.additionalHeaders = new HashMap<>(0);
  }

  public RisRequestSpec get() {
    this.httpMethod = GET;
    return this;
  }

  public RisRequestSpec put() {
    this.httpMethod = PUT;
    return this;
  }

  public RisRequestSpec delete() {
    this.httpMethod = DELETE;
    return this;
  }

  public RisRequestSpec post() {
    this.httpMethod = POST;
    return this;
  }

  public RisRequestSpec patch() {
    this.httpMethod = PATCH;
    return this;
  }

  public RisRequestSpec uri(String uri) {
    this.uri = UriComponentsBuilder.fromUriString(uri).buildAndExpand().encode().toUri();
    return this;
  }

  public RisRequestSpec uri(URI uri) {
    this.uri = uri;
    return this;
  }

  public <T> RisRequestSpec bodyValue(T body) {
    this.bodySupplier = () -> body;
    return this;
  }

  public RisRequestSpec bodyJsonString(String json) {
    this.json = json;
    return this;
  }

  public RisRequestSpec bodyAsByteArray(byte[] bytes) {
    this.bodyAsBytes = bytes;
    return this;
  }

  public RisRequestSpec contentType(MediaType mediaType) {
    this.mediaType = mediaType;
    return this;
  }

  public RisRequestSpec addHeader(String headerKey, String headerValue) {
    this.additionalHeaders.put(headerKey, headerValue);
    return this;
  }

  public RisRequestSpec addFile(MockMultipartFile file) {
    this.file = file;
    return this;
  }

  public RisResponseSpec exchange() {
    MockHttpServletRequestBuilder request = null;
    MockMultipartHttpServletRequestBuilder multipartRequest = null;

    if (file != null) {
      MockMultipartHttpServletRequestBuilder tempMultipartBuilder =
          MockMvcRequestBuilders.multipart(uri).file(file);
      if (HttpMethod.PUT.equals(httpMethod)) {
        // multipart builders use POST by default; to support PUT we override the method
        tempMultipartBuilder.with(
            req -> {
              req.setMethod("PUT");
              return req;
            });
      }
      multipartRequest = tempMultipartBuilder;
    } else {
      if (httpMethod.equals(GET)) {
        request = MockMvcRequestBuilders.get(uri);
      } else if (httpMethod.equals(PUT)) {
        request = MockMvcRequestBuilders.put(uri);
      } else if (httpMethod.equals(POST)) {
        request = MockMvcRequestBuilders.post(uri);
      } else if (httpMethod.equals(DELETE)) {
        request = MockMvcRequestBuilders.delete(uri);
      } else if (httpMethod.equals(PATCH)) {
        request = MockMvcRequestBuilders.patch(uri);
      } else {
        throw new IllegalStateException("Unsupported HTTP method: " + httpMethod);
      }
    }

    if (multipartRequest == null) {
      applyCommon(request);

      try {
        ResultActions resultActions = mockMvc.perform(request);
        return new RisResponseSpec(resultActions, objectMapper);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      applyCommon(multipartRequest);

      try {
        ResultActions resultActions = mockMvc.perform(multipartRequest);
        return new RisResponseSpec(resultActions, objectMapper);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void applyCommon(MockHttpServletRequestBuilder req) {
    if (csrfCookie != null) {
      req.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
    } else {
      req.with(csrf());
    }

    if (login != null) {
      req.with(login);
    }

    if (!additionalHeaders.isEmpty()) {
      additionalHeaders.forEach(req::header);
    }

    req.contentType(Objects.requireNonNullElse(mediaType, MediaType.APPLICATION_JSON));

    if (bodySupplier != null) {
      try {
        String jsonString = objectMapper.writeValueAsString(bodySupplier.get());
        req.content(jsonString);
      } catch (JacksonException e) {
        throw new RuntimeException(e);
      }
    } else if (json != null) {
      req.content(json);
    } else if (bodyAsBytes != null) {
      req.content(bodyAsBytes);
    }
  }

  private void applyCommon(MockMultipartHttpServletRequestBuilder req) {
    if (csrfCookie != null) {
      req.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
    } else {
      req.with(csrf());
    }

    if (login != null) {
      req.with(login);
    }

    if (!additionalHeaders.isEmpty()) {
      additionalHeaders.forEach(req::header);
    }

    req.contentType(Objects.requireNonNullElse(mediaType, MediaType.APPLICATION_JSON));

    if (bodySupplier != null) {
      try {
        String jsonString = objectMapper.writeValueAsString(bodySupplier.get());
        req.content(jsonString);
      } catch (JacksonException e) {
        throw new RuntimeException(e);
      }
    } else if (json != null) {
      req.content(json);
    } else if (bodyAsBytes != null) {
      req.content(bodyAsBytes);
    }

    if (file != null) {
      req.file(file);
    }
  }
}
