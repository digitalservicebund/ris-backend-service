package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.utils.S3AsyncMockClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class OtcObsConfig {
  @Value("${otc.obs.endpoint:https://obs.eu-de.otc.t-systems.com}")
  private String endpoint;

  @Value("${otc.obs.accessKeyId:test}")
  private String accessKeyId;

  @Value("${otc.obs.secretAccessKey:test}")
  private String secretAccessKey;

  @Bean
  @Profile({"production", "staging"})
  public S3AsyncClient amazonS3Async() throws URISyntaxException {
    SdkAsyncHttpClient httpClient =
        NettyNioAsyncHttpClient.builder().writeTimeout(Duration.ZERO).maxConcurrency(64).build();

    return S3AsyncClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
        .endpointOverride(new URI(endpoint))
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
