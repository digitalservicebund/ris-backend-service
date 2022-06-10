package de.bund.digitalservice.ris.utils;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

public class S3AsyncMockClient implements S3AsyncClient {

  @Override
  public String serviceName() {
    return null;
  }

  @Override
  public void close() {
    /* this method is empty because of mock */
  }

  @Override
  public CompletableFuture<PutObjectResponse> putObject(
      PutObjectRequest putObjectRequest, AsyncRequestBody requestBody) {
    return CompletableFuture.completedFuture(PutObjectResponse.builder().build());
  }

  @Override
  public CompletableFuture<ListObjectsV2Response> listObjectsV2(
      ListObjectsV2Request listObjectsV2Request) {
    return CompletableFuture.completedFuture(ListObjectsV2Response.builder().build());
  }

  @Override
  public <T> CompletableFuture<T> getObject(
      GetObjectRequest getObjectRequest,
      AsyncResponseTransformer<GetObjectResponse, T> asyncResponseTransformer) {
    return CompletableFuture.completedFuture(
        (T) ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] {}));
  }

  @Override
  public CompletableFuture<DeleteObjectResponse> deleteObject(
      DeleteObjectRequest deleteObjectRequest) {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
