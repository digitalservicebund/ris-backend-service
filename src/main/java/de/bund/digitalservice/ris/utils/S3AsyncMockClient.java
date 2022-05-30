package de.bund.digitalservice.ris.utils;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class S3AsyncMockClient implements S3AsyncClient {

  @Override
  public String serviceName() {
    return null;
  }

  @Override
  public void close() {}

  @Override
  public CompletableFuture<PutObjectResponse> putObject(
      PutObjectRequest putObjectRequest, AsyncRequestBody requestBody) {
    return CompletableFuture.completedFuture(PutObjectResponse.builder().build());
  }
}
