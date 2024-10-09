package de.bund.digitalservice.ris.caselaw.adapter;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class CaseLawBucket extends S3Bucket {

  public CaseLawBucket(
      @Qualifier("caseLawS3Client") S3Client s3Client,
      @Value("${s3.file-storage.case-law.bucket-name}") String bucketName) {
    super(s3Client, bucketName, LogManager.getLogger(CaseLawBucket.class));
  }
}
