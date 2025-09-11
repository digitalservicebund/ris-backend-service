package de.bund.digitalservice.ris.caselaw.adapter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
@Profile("production")
public class PrototypePortalBucket extends S3Bucket {

  public PrototypePortalBucket(
      @Qualifier("portalS3Client") S3Client s3Client,
      @Value("${s3.file-storage.case-law-prototype.bucket-name:no-bucket}") String bucketName) {
    super(s3Client, bucketName);
  }
}
