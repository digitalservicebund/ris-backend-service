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
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
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
  private String portalAccessKeyId;

  @Value("${s3.file-storage.case-law.secret-access-key:test}")
  private String portalSecretAccessKey;

  @Value("${s3.file-storage.case-law-prototype.access-key-id:test}")
  private String prototypePortalAccessKeyId;

  @Value("${s3.file-storage.case-law-prototype.access-key:test}")
  private String prototypePortalAccessKey;

  @Bean(name = "docxS3Client")
  @Profile({"staging", "production", "uat"})
  public S3Client docxS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(docxAccessKeyId, docxSecretAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .build();
  }

  @Bean(name = "portalS3Client")
  @Profile({"staging", "uat"})
  public S3Client portalS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(portalAccessKeyId, portalSecretAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .build();
  }

  @Bean(name = "portalS3Client")
  @Profile({"production"})
  public S3Client portalS3NoopClient() {
    return new S3NoOpClient();
  }

  @Bean(name = "prototypePortalS3Client")
  @Profile({"staging", "production"})
  public S3Client prototypePortalS3Client() throws URISyntaxException {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(prototypePortalAccessKeyId, prototypePortalAccessKey)))
        .endpointOverride(new URI(endpoint))
        .region(Region.of(EU_DE))
        .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .build();
  }

  @Bean(name = "prototypePortalS3Client")
  @Profile({"uat"})
  public S3Client prototypePortalS3NoopClient() {
    return new S3NoOpClient();
  }

  @Bean(name = "docxS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client docxS3MockClient() {
    return new S3MockClient();
  }

  @Bean(name = "portalS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client portalS3MockClient() {
    return new S3MockClient();
  }

  @Bean(name = "prototypePortalS3Client")
  @Profile({"!production & !staging & !uat"})
  public S3Client prototypePortalS3MockClient() {
    return new S3MockClient();
  }
}
