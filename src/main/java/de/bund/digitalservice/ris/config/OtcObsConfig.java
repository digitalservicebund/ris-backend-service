package de.bund.digitalservice.ris.config;

import de.bund.digitalservice.ris.utils.S3AsyncMockClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class OtcObsConfig {
  @Value("${otc.obs.url}:https://obs.eu-de.otc.t-systems.com")
  private String otcObsUrlString;

  @Bean
  @Profile({"production", "staging"})
  public S3AsyncClient amazonS3Async() throws URISyntaxException {
    SdkAsyncHttpClient httpClient =
        NettyNioAsyncHttpClient.builder().writeTimeout(Duration.ZERO).maxConcurrency(64).build();

    return S3AsyncClient.builder()
        .endpointOverride(new URI(otcObsUrlString))
        .region(Region.of("eu-de"))
        .httpClient(httpClient)
        .build();
  }

  @Bean
  @Profile({"!production & !staging"})
  public S3AsyncClient amazonS3AsyncMock() {
    return new S3AsyncMockClient();
  }
}
