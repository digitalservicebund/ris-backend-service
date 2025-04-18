package de.bund.digitalservice.ris.caselaw.adapter;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.config.location=classpath:application-uat.yaml"})
public class DocumentNumberPatternConfigUATTest extends DocumentNumberPatternConfigTest {}
