package de.bund.digitalservice.ris.controller;

import de.bund.digitalservice.ris.service.DocUnitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocUnitController.class)
@WithMockUser
class DocUnitControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private DocUnitService service;

    @Test
    public void testUploadFile() {
        var bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("fileToUpload", new byte[] {})
                .header("Content-Disposition", "form-data; name=fileToUpload; filename=test.docx");

        webClient.mutateWith(csrf()).post()
                .uri("/api/v1/docunit/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();
    }
}
