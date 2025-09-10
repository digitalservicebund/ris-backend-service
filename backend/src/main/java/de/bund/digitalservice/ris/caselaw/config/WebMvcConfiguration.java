package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.UserMethodArgumentResolver;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
  private final ObjectMapper objectMapper;
  private final UserService userService;

  public WebMvcConfiguration(ObjectMapper objectMapper, UserService userService) {
    this.objectMapper = objectMapper;
    this.userService = userService;
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(byteArrayHttpMessageConverter());
    converters.add(stringHttpMessageConverter());
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new UserMethodArgumentResolver(userService));
  }

  private StringHttpMessageConverter stringHttpMessageConverter() {
    StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
    List<MediaType> supportedMediaTypes =
        new ArrayList<>(MediaType.parseMediaTypes("application/openmetrics-text"));
    supportedMediaTypes.add(MediaType.TEXT_PLAIN);
    supportedMediaTypes.add(MediaType.ALL);
    stringHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
    stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
    return stringHttpMessageConverter;
  }

  private ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
    ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
    List<MediaType> supportedMediaType =
        new ArrayList<>(
            MediaType.parseMediaTypes(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    supportedMediaType.add(MediaType.APPLICATION_OCTET_STREAM);
    supportedMediaType.add(MediaType.ALL);
    arrayHttpMessageConverter.setSupportedMediaTypes(supportedMediaType);
    return arrayHttpMessageConverter;
  }
}
