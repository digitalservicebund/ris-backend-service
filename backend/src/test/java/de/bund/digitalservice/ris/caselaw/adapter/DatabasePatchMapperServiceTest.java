package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.PathValueOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DatabasePatchMapperServiceTest {

  private AttachmentService attachmentService;
  private DatabasePatchMapperService service;

  private static final String BASE64_IMAGE =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA"
          + "AAAFCAYAAACNbyblAAAAHElEQVQI12P4"
          + "//8/w38GIAXDIBKE0DHxgljNBAAO"
          + "9TXL0Y4OHwAAAABJRU5ErkJggg==";

  public static Element getDefaultImageTag() {
    return new Element("img")
        .addClass("inline")
        .addClass("align-baseline")
        .attr("src", BASE64_IMAGE)
        .attr("alt", "Smallest base64 image")
        .attr("width", "82")
        .attr("height", "80");
  }

  @BeforeEach
  void setup() {
    attachmentService = mock(AttachmentService.class);
    DatabaseDocumentationUnitPatchRepository repository =
        mock(DatabaseDocumentationUnitPatchRepository.class);
    service = new DatabasePatchMapperService(new ObjectMapper(), repository, attachmentService);
  }

  @Test
  void extractAndStoreBase64Images_shouldExtractAndReplaceBase64ImageWithApiUrl() {
    String html = "<p>" + getDefaultImageTag().outerHtml() + "</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYTestDoc0001").build();

    Attachment attachment =
        Attachment.builder().name("2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png").build();

    when(attachmentService.attachFileToDocumentationUnit(
            any(), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(attachment);

    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String newHtml = ((PathValueOperation) op).getValue().textValue();

    assertThat(newHtml).doesNotContain("data:image");

    assertEquals(
        "<p><img class=\"inline align-baseline\" src=\"/api/v1/caselaw/documentunits/YYTestDoc0001/image/2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png\" alt=\"Smallest base64 image\" width=\"82\" height=\"80\"></p>",
        newHtml);
  }

  @Test
  void extractAndStoreBase64Images_withSpecialCharactersInText_shouldKeepText() {
    String html =
        "<p>This is & and this is <" + getDefaultImageTag().outerHtml() + " and this ></p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYTestDoc0001").build();

    Attachment attachment =
        Attachment.builder().name("2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png").build();

    when(attachmentService.attachFileToDocumentationUnit(
            any(), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(attachment);

    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String newHtml = ((PathValueOperation) op).getValue().textValue();

    assertThat(newHtml).doesNotContain("data:image");

    assertEquals(
        "<p>This is & and this is <<img class=\"inline align-baseline\" src=\"/api/v1/caselaw/documentunits/YYTestDoc0001/image/2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png\" alt=\"Smallest base64 image\" width=\"82\" height=\"80\"> and this ></p>",
        newHtml);
  }

  @Test
  void extractAndStoreBase64Images_shouldKeepOperationsWithoutImages() {
    final String html = "<p>Content without an image tag</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYNoImageDoc0002").build();

    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String resultHtml = ((PathValueOperation) op).getValue().textValue();

    assertEquals(html, resultHtml);
  }

  @Test
  void extractAndStoreBase64Images_withoutImageTagsAndWithSpecialCharactersInText_shouldKeepText() {
    final String html = "<p>Content without an image tag but with & and with < and >!</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYNoImageDoc0002").build();

    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String resultHtml = ((PathValueOperation) op).getValue().textValue();

    assertEquals(html, resultHtml);
  }
}
