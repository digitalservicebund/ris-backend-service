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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class S3MockClient implements S3Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3MockClient.class);

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

    return (T) ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), bytes);
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
    Path multipartBase = localStorageDirectory.resolve(".multipart").resolve(uploadId);
    try {
      Files.createDirectories(multipartBase);
      // store the target key so we can validate on complete
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
            .resolve(".multipart")
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
    Path multipartBase = localStorageDirectory.resolve(".multipart").resolve(uploadId);
    Path targetFile = localStorageDirectory.resolve(request.key());

    // Ensure parent directories exist
    targetFile.toFile().getParentFile().mkdirs();

    try (FileOutputStream fos = new FileOutputStream(targetFile.toFile(), false)) {
      CompletedMultipartUpload multipart = request.multipartUpload();
      // parts() may return an unmodifiable list from the SDK; copy into a mutable list
      List<CompletedPart> parts = new java.util.ArrayList<>(multipart.parts());
      // sort parts by partNumber to ensure correct order
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

      // cleanup multipart temp files after successful completion
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
    Path multipartBase = localStorageDirectory.resolve(".multipart").resolve(uploadId);
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
