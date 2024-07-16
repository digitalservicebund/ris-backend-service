package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.S3MockClient;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class OtcObsConfig {
  @Value("${otc.obs.endpoint:https://obs.eu-de.otc.t-systems.com}")
  private String endpoint;

  @Value("${otc.obs.accessKeyId:test}")
  private String accessKeyId;

  @Value("${otc.obs.secretAccessKey:test}")
  private String secretAccessKey;

  @Bean
  @Profile({"staging", "production", "uat"})
  public S3Client amazonS3() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of("eu-de"))
        .build();
  }

  @Bean
  @Profile({"!production & !staging & !uat"})
  public S3Client amazonS3Mock() {
    return new S3MockClient();
  }
}
