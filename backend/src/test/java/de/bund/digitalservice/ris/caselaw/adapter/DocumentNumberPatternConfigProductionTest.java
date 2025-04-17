package de.bund.digitalservice.ris.caselaw.adapter;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.config.location=classpath:application-production.yaml"})
public class DocumentNumberPatternConfigProductionTest extends DocumentNumberPatternConfigTest {}
