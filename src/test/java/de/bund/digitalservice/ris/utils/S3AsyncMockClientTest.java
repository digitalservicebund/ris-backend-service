package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.bund.digitalservice.ris.domain.DocUnitService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.model.*;

@ExtendWith(SpringExtension.class)
@Import(S3AsyncMockClient.class)
@TestPropertySource(properties = "local.file-storage:local-storage")
class S3AsyncMockClientTest {

  @Autowired S3AsyncMockClient client;

  @Test
  void testS3AsyncMockClient_serviceName() {
    assertNull(client.serviceName());
  }

  @Test
  @Ignore
  void testS3AsyncMockClient_putObject() throws ExecutionException, InterruptedException {
    CompletableFuture<PutObjectResponse> expected =
        CompletableFuture.completedFuture(PutObjectResponse.builder().build());

    assertEquals(
        expected.get(),
        client.putObject(PutObjectRequest.builder().build(), AsyncRequestBody.empty()).get());
  }

  @Test
  @Ignore
  void testS3AsyncMockClient_listObjectsV2() throws ExecutionException, InterruptedException {
    CompletableFuture<ListObjectsV2Response> expected =
        CompletableFuture.completedFuture(ListObjectsV2Response.builder().build());

    assertEquals(
        expected.get(), client.listObjectsV2(ListObjectsV2Request.builder().build()).get());
  }

  @Test
  @Ignore
  void testS3AsyncMockClient_getObject() throws ExecutionException, InterruptedException {
    CompletableFuture<ResponseBytes<GetObjectResponse>> expected =
        CompletableFuture.completedFuture(
            ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] {}));

    assertEquals(
        expected.get(),
        client
            .getObject(GetObjectRequest.builder().build(), AsyncResponseTransformer.toBytes())
            .get());
  }
}
