package de.bund.digitalservice.ris.caselaw.adapter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Bucket {

  private final Logger logger;
  private final S3Client s3Client;
  private final String bucketName;

  public S3Bucket(S3Client s3Client, String bucketName, Logger logger) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.logger = logger;
  }

  public List<String> getAllFilenames() {
    return getAllFilenamesByPath("");
  }

  public List<String> getAllFilenamesByPath(String path) {
    List<String> keys = new ArrayList<>();
    ListObjectsV2Response response;
    ListObjectsV2Request request =
        ListObjectsV2Request.builder().bucket(bucketName).prefix(path).build();
    do {
      response = s3Client.listObjectsV2(request);
      keys.addAll(response.contents().stream().map(S3Object::key).toList());
      String token = response.nextContinuationToken();
      request = ListObjectsV2Request.builder().bucket(bucketName).continuationToken(token).build();
    } while (Boolean.TRUE.equals(response.isTruncated()));

    return keys;
  }

  public Optional<String> getFileAsString(String filename) {
    Optional<byte[]> s3Response = get(filename);
    return s3Response.map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  public Optional<byte[]> get(String eli) {
    try {
      var request = GetObjectRequest.builder().bucket(bucketName).key(eli).build();
      var response = s3Client.getObject(request);
      return Optional.of(response.readAllBytes());
    } catch (NoSuchKeyException e) {
      logger.error(String.format("Object key %s does not exist", eli), e);
    } catch (S3Exception e) {
      logger.error("AWS S3 encountered an issue: {}", e.awsErrorDetails().errorMessage());
    } catch (Exception e) {
      logger.error("An error occurred: {}", e.getMessage());
    }
    return Optional.empty();
  }

  public void delete(String fileName) {
    s3Client.deleteObject(builder -> builder.bucket(bucketName).key(fileName));
  }

  public void save(String fileName, String fileContent) {
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
    s3Client.putObject(putObjectRequest, RequestBody.fromString(fileContent));
  }

  public void close() {
    s3Client.close();
  }
}
