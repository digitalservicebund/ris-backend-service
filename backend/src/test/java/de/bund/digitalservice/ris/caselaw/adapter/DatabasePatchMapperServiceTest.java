package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.PathValueOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
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
    // Arrange
    String textWithSpecialCharacters =
        "< & > Test-Case_01 Dev@Ops2025' User*Role+Admin Security~Patch_#9 Prod:Release;V2.0 ?";
    String html = "<p>" + textWithSpecialCharacters + getDefaultImageTag().outerHtml() + "</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYTestDoc0001").build();

    Attachment attachment =
        Attachment.builder().name("2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png").build();

    when(attachmentService.attachFileToDocumentationUnit(
            any(), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(attachment);

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String newHtml = ((PathValueOperation) op).getValue().textValue();

    assertThat(newHtml).doesNotContain("data:image");

    assertEquals(
        "<p>"
            + textWithSpecialCharacters
            + "<img class=\"inline align-baseline\" src=\"/api/v1/caselaw/documentunits/YYTestDoc0001/image/2e7e7908-faa3-4aca-a0ff-4bfa4dcf316a.png\" alt=\"Smallest base64 image\" width=\"82\" height=\"80\"></p>",
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
    // Arrange
    String textWithSpecialCharacters =
        "< & > Test-Case_01 Dev@Ops2025' User*Role+Admin Security~Patch_#9 Prod:Release;V2.0 ?";
    final String html =
        "<p>Content without an image tag but with special characters: "
            + textWithSpecialCharacters
            + "</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYNoImageDoc0002").build();

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(ReplaceOperation.class);

    String resultHtml = ((PathValueOperation) op).getValue().textValue();

    assertEquals(html, resultHtml);
  }

  @Test
  void extractAndStoreBase64Images_withMultipleImageTagsAndSpecialCharacters_shouldKeepText() {
    // Arrange
    String textWithSpecialCharacters =
        "< & > Test-Case_01 Dev@Ops2025' User*Role+Admin Security~Patch_#9 Prod:Release;V2.0 ?";
    String html =
        "<p>"
            + getDefaultImageTag().outerHtml()
            + textWithSpecialCharacters
            + getDefaultImageTag().outerHtml()
            + "</p>";
    JsonPatch patch = new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYMultipleImagesDoc").build();

    Attachment attachment = Attachment.builder().name("testfile.png").build();
    when(attachmentService.attachFileToDocumentationUnit(
            any(), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(attachment);

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    String resultHtml = ((PathValueOperation) result.getOperations().get(0)).getValue().textValue();
    assertThat(resultHtml).doesNotContain("data:image");
    assertThat(
            StringUtils.countMatches(
                resultHtml,
                "src=\"/api/v1/caselaw/documentunits/YYMultipleImagesDoc/image/testfile.png\""))
        .isEqualTo(2);
  }

  @Test
  void extractAndStoreBase64Images_withAddOperation_shouldReturnAddOperation() {
    // Arrange
    String html = "<p>" + getDefaultImageTag().outerHtml() + "</p>";
    JsonPatch patch = new JsonPatch(List.of(new AddOperation("/foo", new TextNode(html))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYAddOpDoc").build();

    Attachment attachment = Attachment.builder().name("testaddop.png").build();

    when(attachmentService.attachFileToDocumentationUnit(
            any(), any(ByteBuffer.class), any(HttpHeaders.class), any()))
        .thenReturn(attachment);

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(AddOperation.class);

    String newHtml = ((PathValueOperation) op).getValue().textValue();
    assertThat(newHtml).doesNotContain("data:image");
    assertThat(newHtml)
        .contains("src=\"/api/v1/caselaw/documentunits/YYAddOpDoc/image/testaddop.png\"");
  }

  @Test
  void extractAndStoreBase64Images_withOtherOperation_shouldReturnOtherOperation() {
    // Arrange
    JsonPatch patch = new JsonPatch(List.of(new RemoveOperation("/foo")));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYAddOpDoc").build();

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).hasSize(1);
    JsonPatchOperation op = result.getOperations().get(0);
    assertThat(op).isInstanceOf(RemoveOperation.class);
    verify(attachmentService, never())
        .attachFileToDocumentationUnit(any(), any(ByteBuffer.class), any(HttpHeaders.class), any());
  }

  @Test
  void extractAndStoreBase64Images_withEmptyPatch_shouldReturnNoOperations() {
    // Arrange
    JsonPatch patch = new JsonPatch(Collections.emptyList());

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYEmptyDoc").build();

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).isEmpty();
  }

  @Test
  void extractAndStoreBase64Images_withInvalidHtml_shouldKeepOriginalText() {
    // Arrange
    String invalidHtml = "<p><b>This is broken";
    JsonPatch patch =
        new JsonPatch(List.of(new ReplaceOperation("/foo", new TextNode(invalidHtml))));

    DocumentationUnit docUnit =
        Decision.builder().uuid(UUID.randomUUID()).documentNumber("YYInvalidHtmlDoc").build();

    // Act
    JsonPatch result = service.extractAndStoreBase64Images(patch, docUnit, User.builder().build());

    // Assert
    assertThat(result.getOperations()).hasSize(1);
    String resultHtml = ((PathValueOperation) result.getOperations().get(0)).getValue().textValue();
    assertEquals(invalidHtml, resultHtml);
  }
}
