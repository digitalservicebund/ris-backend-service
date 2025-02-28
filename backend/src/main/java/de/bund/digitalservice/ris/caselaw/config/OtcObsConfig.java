package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.S3MockClient;
import de.bund.digitalservice.ris.caselaw.adapter.S3NoOpClient;
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
  public static final String EU_DE = "eu-de";

  @Value("${otc.obs.endpoint:https://obs.eu-de.otc.t-systems.com}")
  private String endpoint;

  @Value("${otc.obs.accessKeyId:test}")
  private String docxAccessKeyId;

  @Value("${otc.obs.secretAccessKey:test}")
  private String docxSecretAccessKey;

  @Value("${s3.file-storage.case-law.access-key-id:test}")
  private String internalPortalAccessKeyId;

  @Value("${s3.file-storage.case-law.secret-access-key:test}")
  private String internalPortalSecretAccessKey;

  @Value("${s3.file-storage.case-law-prototype.access-key-id:test}")
  private String publicPortalAccessKeyId;

  @Value("${s3.file-storage.case-law-prototype.access-key:test}")
  private String publicPortalAccessKey;

  @Bean(name = "docxS3Client")
  @Profile({"staging", "production", "uat"})
  public S3Client docxS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(docxAccessKeyId, docxSecretAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .build();
  }

  @Bean(name = "internalPortalS3Client")
  @Profile({"staging"})
  public S3Client internalPortalS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    internalPortalAccessKeyId, internalPortalSecretAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .build();
  }

  @Bean(name = "internalPortalS3Client")
  @Profile({"production", "uat"})
  public S3Client internalPortalS3NoopClient() {
    return new S3NoOpClient();
  }

  @Bean(name = "publicPortalS3Client")
  @Profile({"staging", "production"})
  public S3Client publicPortalS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(publicPortalAccessKeyId, publicPortalAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .build();
  }

  @Bean(name = "publicPortalS3Client")
  @Profile({"uat"})
  public S3Client publicPortalS3NoopClient() {
    return new S3NoOpClient();
  }

  @Bean(name = "docxS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client docxS3MockClient() {
    return new S3MockClient();
  }

  @Bean(name = "internalPortalS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client internalPortalS3MockClient() {
    return new S3MockClient();
  }

  @Bean(name = "publicPortalS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client publicPortalS3MockClient() {
    return new S3MockClient();
  }
}
