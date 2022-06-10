package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.model.*;

class S3AsyncMockClientTest {
  @Test
  void testS3AsyncMockClient_serviceName() throws ExecutionException, InterruptedException {
    try (var client = new S3AsyncMockClient()) {
      assertNull(client.serviceName());
    }
  }

  @Test
  void testS3AsyncMockClient_putObject() throws ExecutionException, InterruptedException {
    CompletableFuture<PutObjectResponse> expected =
        CompletableFuture.completedFuture(PutObjectResponse.builder().build());

    try (var client = new S3AsyncMockClient()) {
      assertEquals(
          expected.get(),
          client.putObject(PutObjectRequest.builder().build(), AsyncRequestBody.empty()).get());
    }
  }

  @Test
  void testS3AsyncMockClient_listObjectsV2() throws ExecutionException, InterruptedException {
    CompletableFuture<ListObjectsV2Response> expected =
        CompletableFuture.completedFuture(ListObjectsV2Response.builder().build());

    try (var client = new S3AsyncMockClient()) {
      assertEquals(
          expected.get(), client.listObjectsV2(ListObjectsV2Request.builder().build()).get());
    }
  }

  @Test
  void testS3AsyncMockClient_getObject() throws ExecutionException, InterruptedException {
    CompletableFuture<ResponseBytes<GetObjectResponse>> expected =
        CompletableFuture.completedFuture(
            ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] {}));

    try (var client = new S3AsyncMockClient()) {
      assertEquals(
          expected.get(),
          client
              .getObject(GetObjectRequest.builder().build(), AsyncResponseTransformer.toBytes())
              .get());
    }
  }
}
