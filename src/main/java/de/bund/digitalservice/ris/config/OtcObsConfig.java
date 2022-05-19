package de.bund.digitalservice.ris.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class OtcObsConfig {
  @Bean
  public S3Client s3Client() throws URISyntaxException {
    return S3Client.builder()
        .endpointOverride(new URI("https://obs.eu-de.otc.t-systems.com"))
        .region(Region.of("eu-de"))
        .build();
  }

  @Bean
  public S3AsyncClient amazonS3Async() throws URISyntaxException {
    SdkAsyncHttpClient httpClient =
        NettyNioAsyncHttpClient.builder().writeTimeout(Duration.ZERO).maxConcurrency(64).build();

    return S3AsyncClient.builder()
        .endpointOverride(new URI("https://obs.eu-de.otc.t-systems.com"))
        .region(Region.of("eu-de"))
        .httpClient(httpClient)
        .build();
  }
}
