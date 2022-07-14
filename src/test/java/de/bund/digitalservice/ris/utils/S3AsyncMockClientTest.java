package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
}
