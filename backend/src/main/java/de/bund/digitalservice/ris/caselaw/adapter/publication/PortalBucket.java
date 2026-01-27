package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.S3Bucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class PortalBucket extends S3Bucket {

  public PortalBucket(
      @Qualifier("portalS3Client") S3Client s3Client,
      @Value("${s3.file-storage.case-law.bucket-name:no-bucket}") String bucketName) {
    super(s3Client, bucketName);
  }
}
