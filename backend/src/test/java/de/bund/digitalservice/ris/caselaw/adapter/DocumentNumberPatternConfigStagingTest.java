package de.bund.digitalservice.ris.caselaw.adapter;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.config.location=classpath:application-staging.yaml"})
public class DocumentNumberPatternConfigStagingTest extends DocumentNumberPatternConfigTest {}
