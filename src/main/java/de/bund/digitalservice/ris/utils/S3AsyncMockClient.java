package de.bund.digitalservice.ris.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private String localFileDirectory;

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

    AtomicBoolean append = new AtomicBoolean(false);
    String fileName = putObjectRequest.key();

    requestBody.subscribe(
        byteBuffer -> {
          try {
            File file = new File(localFileDirectory + File.separator + fileName);
            FileChannel channel = new FileOutputStream(file, append.get()).getChannel();
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
    File localFileStorage = new File(localFileDirectory);
    if (localFileStorage.isDirectory()) {
      nameList = localFileStorage.list();
    }

    var objectList =
        Arrays.stream(nameList).map(name -> S3Object.builder().key(name).build()).toList();
    return CompletableFuture.completedFuture(
        ListObjectsV2Response.builder().contents(objectList).build());
  }

  @Override
  public <T> CompletableFuture<T> getObject(
      GetObjectRequest getObjectRequest,
      AsyncResponseTransformer<GetObjectResponse, T> asyncResponseTransformer) {

    byte[] bytes = new byte[] {};

    try {
      String fileName = getObjectRequest.key();
      File file = new File(localFileDirectory + File.separator + fileName);
      FileInputStream fl = new FileInputStream(file);
      bytes = new byte[(int) file.length()];
      fl.read(bytes);
      fl.close();
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
    File file = new File(localFileDirectory + File.separator + fileName);
    if (file.exists()) {
      file.delete();
    }

    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
