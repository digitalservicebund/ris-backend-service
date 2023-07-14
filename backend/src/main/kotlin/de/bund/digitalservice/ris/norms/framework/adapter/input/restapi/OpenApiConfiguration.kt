package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
    companion object {
        const val NORMS_TAG = "Norms"
        const val CASELAW_TAG = "Caselaw"
    }

    @Bean
    fun customOpenAPI(): OpenAPI? {
        return OpenAPI().info(
            Info()
                .title("NeuRIS Service API")
                .version("0.0.1")
                .description("Interface for the NeuRIS service (legal information system)")
                .contact(Contact().name("DigitalService GmbH des Bundes").url("https://digitalservice.bund.de").email("hallo@digitalservice.bund.de")),
//                .license(License().name("MIT License").url("https://mit-license.org")),
        )
            .addServersItem(Server().description("Public production server").url("https://ris.prod.ds4g.net"))
            .addServersItem(Server().description("Local development server").url("http://127.0.0.1"))
            .addTagsItem(Tag().name(NORMS_TAG).description("Endpoints regarding norms"))
            .addTagsItem(Tag().name(CASELAW_TAG).description("Endpoints regarding caselaw"))
    }
}
