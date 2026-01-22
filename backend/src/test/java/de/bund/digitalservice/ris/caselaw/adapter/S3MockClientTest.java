package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

class S3MockClientTest {

  private S3MockClient client;
  private Path tempDir;

  @BeforeEach
  void setUp() throws Exception {
    tempDir = Files.createTempDirectory("s3mocktest");
    client = new S3MockClient();

    Field f = S3MockClient.class.getDeclaredField("relativeLocalStorageDirectory");
    f.setAccessible(true);
    f.set(client, tempDir);

    client.init();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (tempDir != null && Files.exists(tempDir)) {
      Files.walkFileTree(
          tempDir,
          new SimpleFileVisitor<>() {
            @Override
            public @NonNull FileVisitResult visitFile(
                @NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public @NonNull FileVisitResult postVisitDirectory(@NonNull Path dir, IOException exc)
                throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }
          });
    }
  }

  @Test
  void putGetDelete_and_listObjectsV2() {
    String key = "some/folder/test.txt";
    byte[] content = "hello s3 mock".getBytes(StandardCharsets.UTF_8);

    PutObjectRequest putReq = PutObjectRequest.builder().key(key).build();
    client.putObject(putReq, RequestBody.fromBytes(content));

    ListObjectsV2Request listReq = ListObjectsV2Request.builder().prefix("some/folder").build();
    var listResp = client.listObjectsV2(listReq);
    List<?> contents = listResp.contents();
    assertFalse(contents.isEmpty());

    boolean found = listResp.contents().stream().anyMatch(o -> o.key().equals("test.txt"));
    assertTrue(found, "expected to find test.txt in listObjectsV2 result");

    GetObjectRequest getReq = GetObjectRequest.builder().key(key).build();
    ResponseTransformer<software.amazon.awssdk.services.s3.model.GetObjectResponse, byte[]>
        transformer =
            new ResponseTransformer<>() {
              @Override
              public byte[] transform(
                  software.amazon.awssdk.services.s3.model.GetObjectResponse response,
                  AbortableInputStream inputStream)
                  throws Exception {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                  inputStream.transferTo(baos);
                  return baos.toByteArray();
                }
              }
            };

    byte[] got = client.getObject(getReq, transformer);
    assertArrayEquals(content, got);

    DeleteObjectRequest delReq = DeleteObjectRequest.builder().key(key).build();
    client.deleteObject(delReq);

    assertThrows(
        NoSuchKeyException.class,
        () -> client.getObject(getReq, transformer),
        "expected NoSuchKeyException after delete");
  }

  @Test
  void multipart_upload_and_complete() {
    String key = "mp/test.bin";

    CreateMultipartUploadRequest createReq =
        CreateMultipartUploadRequest.builder().key(key).build();
    var createResp = client.createMultipartUpload(createReq);
    String uploadId = createResp.uploadId();
    assertNotNull(uploadId);

    byte[] part1 = "part-one-contents".getBytes(StandardCharsets.UTF_8);
    byte[] part2 = "part-two-contents".getBytes(StandardCharsets.UTF_8);

    UploadPartRequest up1Req = UploadPartRequest.builder().uploadId(uploadId).partNumber(1).build();
    var up1Resp = client.uploadPart(up1Req, RequestBody.fromBytes(part1));
    String etag1 = up1Resp.eTag();
    assertNotNull(etag1);

    UploadPartRequest up2Req = UploadPartRequest.builder().uploadId(uploadId).partNumber(2).build();
    var up2Resp = client.uploadPart(up2Req, RequestBody.fromBytes(part2));
    String etag2 = up2Resp.eTag();
    assertNotNull(etag2);

    CompletedPart cp1 = CompletedPart.builder().partNumber(1).eTag(etag1).build();
    CompletedPart cp2 = CompletedPart.builder().partNumber(2).eTag(etag2).build();
    CompletedMultipartUpload multipart = CompletedMultipartUpload.builder().parts(cp1, cp2).build();

    CompleteMultipartUploadRequest completeReq =
        CompleteMultipartUploadRequest.builder()
            .uploadId(uploadId)
            .key(key)
            .multipartUpload(multipart)
            .build();

    client.completeMultipartUpload(completeReq);

    GetObjectRequest getReq = GetObjectRequest.builder().key(key).build();
    ResponseTransformer<software.amazon.awssdk.services.s3.model.GetObjectResponse, byte[]>
        transformer =
            new ResponseTransformer<>() {
              @Override
              public byte[] transform(
                  software.amazon.awssdk.services.s3.model.GetObjectResponse response,
                  AbortableInputStream inputStream)
                  throws Exception {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                  inputStream.transferTo(baos);
                  return baos.toByteArray();
                }
              }
            };

    byte[] finalContent = client.getObject(getReq, transformer);
    byte[] expected = new byte[part1.length + part2.length];
    System.arraycopy(part1, 0, expected, 0, part1.length);
    System.arraycopy(part2, 0, expected, part1.length, part2.length);

    assertArrayEquals(expected, finalContent);
  }

  @Test
  void uploadPart_returnsCorrectMd5Etag() throws Exception {
    String key = "md5/test.bin";
    CreateMultipartUploadRequest createReq =
        CreateMultipartUploadRequest.builder().key(key).build();
    var createResp = client.createMultipartUpload(createReq);
    String uploadId = createResp.uploadId();

    byte[] part = "hello-md5".getBytes(StandardCharsets.UTF_8);
    UploadPartRequest upReq = UploadPartRequest.builder().uploadId(uploadId).partNumber(1).build();
    var upResp = client.uploadPart(upReq, RequestBody.fromBytes(part));

    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(part);
    byte[] digest = md.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
      sb.append(String.format("%02x", b));
    }
    String expectedEtag = sb.toString();

    assertEquals(expectedEtag, upResp.eTag());
  }

  @Test
  void completeMultipart_withMissingPart_assemblesOnlyExistingParts() {
    String key = "partial/test.bin";
    CreateMultipartUploadRequest createReq =
        CreateMultipartUploadRequest.builder().key(key).build();
    var createResp = client.createMultipartUpload(createReq);
    String uploadId = createResp.uploadId();

    byte[] part1 = "first-part".getBytes(StandardCharsets.UTF_8);

    UploadPartRequest up1Req = UploadPartRequest.builder().uploadId(uploadId).partNumber(1).build();
    var up1Resp = client.uploadPart(up1Req, RequestBody.fromBytes(part1));

    CompletedPart cp1 = CompletedPart.builder().partNumber(1).eTag(up1Resp.eTag()).build();
    CompletedPart cp2 = CompletedPart.builder().partNumber(2).eTag("nonexistent").build();
    CompletedMultipartUpload multipart = CompletedMultipartUpload.builder().parts(cp1, cp2).build();

    CompleteMultipartUploadRequest completeReq =
        CompleteMultipartUploadRequest.builder()
            .uploadId(uploadId)
            .key(key)
            .multipartUpload(multipart)
            .build();

    client.completeMultipartUpload(completeReq);

    GetObjectRequest getReq = GetObjectRequest.builder().key(key).build();
    ResponseTransformer<software.amazon.awssdk.services.s3.model.GetObjectResponse, byte[]>
        transformer =
            new ResponseTransformer<>() {
              @Override
              public byte[] transform(
                  software.amazon.awssdk.services.s3.model.GetObjectResponse response,
                  AbortableInputStream inputStream)
                  throws Exception {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                  inputStream.transferTo(baos);
                  return baos.toByteArray();
                }
              }
            };

    byte[] finalContent = client.getObject(getReq, transformer);
    assertArrayEquals(part1, finalContent);
  }

  @Test
  void abortMultipartUpload_cleansUpTempFiles() {
    String key = "abort/test.bin";
    CreateMultipartUploadRequest createReq =
        CreateMultipartUploadRequest.builder().key(key).build();
    var createResp = client.createMultipartUpload(createReq);
    String uploadId = createResp.uploadId();

    byte[] part = "to-be-aborted".getBytes(StandardCharsets.UTF_8);
    UploadPartRequest upReq = UploadPartRequest.builder().uploadId(uploadId).partNumber(1).build();
    client.uploadPart(upReq, RequestBody.fromBytes(part));

    Path multipartBase = tempDir.resolve(S3MockClient.MULTIPART).resolve(uploadId);
    assertTrue(Files.exists(multipartBase));

    client.abortMultipartUpload(
        software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest.builder()
            .uploadId(uploadId)
            .build());

    assertFalse(Files.exists(multipartBase));
  }

  @Test
  void listObjectsV2_withoutPrefix_listsRootFiles() {
    String key1 = "root1.txt";
    String key2 = "root2.txt";
    byte[] content = "root-content".getBytes(StandardCharsets.UTF_8);

    client.putObject(PutObjectRequest.builder().key(key1).build(), RequestBody.fromBytes(content));
    client.putObject(PutObjectRequest.builder().key(key2).build(), RequestBody.fromBytes(content));

    ListObjectsV2Request listReq = ListObjectsV2Request.builder().build();
    var listResp = client.listObjectsV2(listReq);
    List<?> contents = listResp.contents();
    assertFalse(contents.isEmpty());

    boolean found1 = listResp.contents().stream().anyMatch(o -> o.key().equals(key1));
    boolean found2 = listResp.contents().stream().anyMatch(o -> o.key().equals(key2));
    assertTrue(found1 && found2);
  }
}
