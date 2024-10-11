package de.bund.digitalservice.ris.caselaw.adapter;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class LdmlBucket extends S3Bucket {

  public LdmlBucket(S3Client s3Client, @Value("${otc.obs.ldml-bucket}") String bucketName) {
    super(s3Client, bucketName, LogManager.getLogger(LdmlBucket.class));
  }
}
