package de.bund.digitalservice.ris.caselaw.adapter;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class S3MockClient implements S3Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3MockClient.class);
  public static final String MULTIPART = ".multipart";

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
  public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody) {

    String fileName = putObjectRequest.key();

    File file = localStorageDirectory.resolve(fileName).toFile();
    file.getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(file, false);
        InputStream inputStream = requestBody.contentStreamProvider().newStream()) {

      byte[] content = new byte[1024];
      int len = -1;
      while ((len = inputStream.read(content)) != -1) {
        fos.write(content, 0, len);
      }
    } catch (IOException ex) {
      LOGGER.info("Couldn't write file: {}", fileName, ex);
    }

    return PutObjectResponse.builder().build();
  }

  @Override
  public ListObjectsV2Response listObjectsV2(ListObjectsV2Request listObjectsV2Request) {

    String[] nameList = null;
    File localFileStorage;
    String prefix = listObjectsV2Request.prefix();
    if (Strings.isNotBlank(prefix)) {
      localFileStorage = localStorageDirectory.resolve(prefix).toFile();
    } else {
      localFileStorage = localStorageDirectory.toFile();
    }

    if (localFileStorage.isDirectory()) {
      nameList = localFileStorage.list();
    }

    List<S3Object> objectList = Collections.emptyList();
    if (nameList != null) {
      objectList =
          Arrays.stream(nameList).map(name -> S3Object.builder().key(name).build()).toList();
    }

    return ListObjectsV2Response.builder().contents(objectList).build();
  }

  @Override
  public <T> T getObject(
      GetObjectRequest getObjectRequest,
      ResponseTransformer<GetObjectResponse, T> responseTransformer) {

    String fileName = getObjectRequest.key();
    File file = localStorageDirectory.resolve(fileName).toFile();

    if (!file.exists()) {
      LOGGER.error("File not found in local storage: {}", file.getAbsolutePath());
      // In a real S3 client, this would throw a NoSuchKeyException
      throw NoSuchKeyException.builder().message("The specified key does not exist.").build();
    }

    GetObjectResponse objectResponse =
        GetObjectResponse.builder()
            .contentLength(file.length())
            .contentType("application/octet-stream")
            .build();

    try (InputStream fileStream = new FileInputStream(file);
        AbortableInputStream abortableInputStream = AbortableInputStream.create(fileStream)) {
      return responseTransformer.transform(objectResponse, abortableInputStream);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to mock S3 getObject for file: " + fileName, e);
    }
  }

  @Override
  public DeleteObjectResponse deleteObject(DeleteObjectRequest deleteObjectRequest) {

    String fileName = deleteObjectRequest.key();
    File file = localStorageDirectory.resolve(fileName).toFile();
    if (file.exists()) {
      try {
        Files.delete(file.toPath());
      } catch (IOException ex) {
        LOGGER.error("Couldn't delete file", ex);
      }
    }

    return DeleteObjectResponse.builder().build();
  }

  // --- Multipart support added below ---

  @Override
  public CreateMultipartUploadResponse createMultipartUpload(CreateMultipartUploadRequest request) {
    String uploadId = UUID.randomUUID().toString();
    Path multipartBase = localStorageDirectory.resolve(MULTIPART).resolve(uploadId);
    try {
      Files.createDirectories(multipartBase);
      Files.writeString(multipartBase.resolve(".key"), request.key());
    } catch (IOException ex) {
      LOGGER.error("Couldn't create multipart upload dir", ex);
    }
    return CreateMultipartUploadResponse.builder().uploadId(uploadId).build();
  }

  @Override
  public UploadPartResponse uploadPart(UploadPartRequest request, RequestBody requestBody) {
    String uploadId = request.uploadId();
    int partNumber = request.partNumber();
    Path partFile =
        localStorageDirectory
            .resolve(MULTIPART)
            .resolve(uploadId)
            .resolve(String.valueOf(partNumber));
    try {
      Files.createDirectories(partFile.getParent());
    } catch (IOException e) {
      LOGGER.error("Couldn't create parent multipart dir", e);
    }

    try (InputStream in = requestBody.contentStreamProvider().newStream();
        FileOutputStream fos = new FileOutputStream(partFile.toFile(), false)) {
      byte[] buffer = new byte[8192];
      int read = -1;
      MessageDigest md = MessageDigest.getInstance("MD5");
      while ((read = in.read(buffer)) != -1) {
        fos.write(buffer, 0, read);
        md.update(buffer, 0, read);
      }
      String etag = bytesToHex(md.digest());
      return UploadPartResponse.builder().eTag(etag).build();
    } catch (NoSuchAlgorithmException ex) {
      LOGGER.error("MD5 algorithm not available", ex);
      return UploadPartResponse.builder().eTag("").build();
    } catch (IOException ex) {
      LOGGER.error("Couldn't write multipart part", ex);
      return UploadPartResponse.builder().eTag("").build();
    }
  }

  @Override
  public CompleteMultipartUploadResponse completeMultipartUpload(
      CompleteMultipartUploadRequest request) {
    String uploadId = request.uploadId();
    Path multipartBase = localStorageDirectory.resolve(MULTIPART).resolve(uploadId);
    Path targetFile = localStorageDirectory.resolve(request.key());

    targetFile.toFile().getParentFile().mkdirs();

    try (FileOutputStream fos = new FileOutputStream(targetFile.toFile(), false)) {
      CompletedMultipartUpload multipart = request.multipartUpload();
      List<CompletedPart> parts = new java.util.ArrayList<>(multipart.parts());
      parts.sort(Comparator.comparingInt(CompletedPart::partNumber));

      for (CompletedPart part : parts) {
        Path partFile = multipartBase.resolve(String.valueOf(part.partNumber()));
        if (!Files.exists(partFile)) {
          LOGGER.warn("Missing part file {} for uploadId {}", partFile, uploadId);
          continue;
        }
        try (FileInputStream fis = new FileInputStream(partFile.toFile())) {
          byte[] buffer = new byte[8192];
          int read = -1;
          while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
          }
        }
      }

      try (java.util.stream.Stream<Path> stream = Files.walk(multipartBase)) {
        stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      } catch (IOException e) {
        LOGGER.warn("Could not clean up multipart temp files for uploadId {}", uploadId, e);
      }

    } catch (IOException ex) {
      LOGGER.error("Couldn't complete multipart upload", ex);
    }

    return CompleteMultipartUploadResponse.builder().build();
  }

  @Override
  public AbortMultipartUploadResponse abortMultipartUpload(AbortMultipartUploadRequest request) {
    String uploadId = request.uploadId();
    Path multipartBase = localStorageDirectory.resolve(MULTIPART).resolve(uploadId);
    try {
      if (Files.exists(multipartBase)) {
        try (java.util.stream.Stream<Path> stream = Files.walk(multipartBase)) {
          stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
      }
    } catch (IOException e) {
      LOGGER.warn("Could not abort multipart upload {}", uploadId, e);
    }
    return AbortMultipartUploadResponse.builder().build();
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
