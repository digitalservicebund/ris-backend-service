package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
  SecurityConfig.class,
  AuthService.class,
  TestConfig.class,
  PatchMapperService.class,
  DocumentNumberPatternConfig.class,
})
public class DocumentUnitControllerTestConfig {}
