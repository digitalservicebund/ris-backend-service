package de.bund.digitalservice.ris.caselaw.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3AsyncMockClient implements S3AsyncClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3AsyncMockClient.class);

  @Value("${local.file-storage}")
  private Path relativeLocalStorageDirectory;

  private Path localStorageDirectory;

  @Override
  public String serviceName() {
    return null;
  }

  @Override
  public void close() {
    /* this method is empty because of mock */
  }

  @PostConstruct
  public void init() {
    this.localStorageDirectory = relativeLocalStorageDirectory.toAbsolutePath();
    this.localStorageDirectory.toFile().mkdirs();
  }

  @Override
  public CompletableFuture<PutObjectResponse> putObject(
      PutObjectRequest putObjectRequest, AsyncRequestBody requestBody) {

    AtomicBoolean append = new AtomicBoolean(false);
    String fileName = putObjectRequest.key();

    requestBody.subscribe(
        byteBuffer -> {
          try (FileOutputStream fos =
              new FileOutputStream(
                  localStorageDirectory.resolve(fileName).toFile(), append.get())) {
            FileChannel channel = fos.getChannel();
            channel.write(byteBuffer);
            channel.close();
            append.set(true);
          } catch (IOException ex) {
            LOGGER.info("Couldn't write file: {}", fileName, ex);
          }
        });

    return CompletableFuture.completedFuture(PutObjectResponse.builder().build());
  }

  @Override
  public CompletableFuture<ListObjectsV2Response> listObjectsV2(
      ListObjectsV2Request listObjectsV2Request) {

    String[] nameList = new String[] {};
    File localFileStorage = localStorageDirectory.toFile();
    if (localFileStorage.isDirectory()) {
      nameList = localFileStorage.list();
    }

    List<S3Object> objectList = Collections.emptyList();
    if (nameList != null) {
      objectList =
          Arrays.stream(nameList).map(name -> S3Object.builder().key(name).build()).toList();
    }

    return CompletableFuture.completedFuture(
        ListObjectsV2Response.builder().contents(objectList).build());
  }

  @Override
  public <T> CompletableFuture<T> getObject(
      GetObjectRequest getObjectRequest,
      AsyncResponseTransformer<GetObjectResponse, T> asyncResponseTransformer) {

    byte[] bytes = new byte[] {};

    String fileName = getObjectRequest.key();
    File file = localStorageDirectory.resolve(fileName).toFile();
    try (FileInputStream fl = new FileInputStream(file)) {
      bytes = new byte[(int) file.length()];
      int readBytes = fl.read(bytes);
      if (readBytes != file.length()) {
        LOGGER.warn("different size between file length and read bytes");
      }
    } catch (IOException ex) {
      LOGGER.error("Couldn't get object from local storage.");
    }

    return CompletableFuture.completedFuture(
        (T) ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), bytes));
  }

  @Override
  public CompletableFuture<DeleteObjectResponse> deleteObject(
      DeleteObjectRequest deleteObjectRequest) {

    String fileName = deleteObjectRequest.key();
    File file = localStorageDirectory.resolve(fileName).toFile();
    if (file.exists()) {
      try {
        Files.delete(file.toPath());
      } catch (IOException ex) {
        LOGGER.error("Couldn't delete file", ex);
      }
    }

    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
