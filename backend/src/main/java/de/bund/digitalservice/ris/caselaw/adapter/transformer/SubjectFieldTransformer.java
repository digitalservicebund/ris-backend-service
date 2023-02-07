package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.NormXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectFieldXml;
import java.util.HashSet;
import java.util.Set;

public class SubjectFieldTransformer {

  public static JPASubjectFieldDTO transformToDTO(SubjectFieldXml subjectFieldXml) {
    return JPASubjectFieldDTO.builder()
        .id(subjectFieldXml.getId())
        .changeDateMail(subjectFieldXml.getChangeDateMail())
        .changeDateClient(subjectFieldXml.getChangeDateClient())
        .changeIndicator(subjectFieldXml.getChangeIndicator())
        .version(subjectFieldXml.getVersion())
        .subjectFieldNumber(subjectFieldXml.getSubjectFieldNumber())
        .subjectFieldText(subjectFieldXml.getSubjectFieldText())
        .navigationTerm(subjectFieldXml.getNavigationTerm())
        .keywords(transformKeywordsToDTOs(subjectFieldXml.getKeywords()))
        .norms(transformNormsToDTOs(subjectFieldXml.getNorms()))
        .build();
  }

  private static Set<JPAKeywordDTO> transformKeywordsToDTOs(Set<String> keywordXmls) {
    if (keywordXmls == null) {
      return null;
    }

    Set<JPAKeywordDTO> jpaKeywordDTOs = new HashSet<>();
    keywordXmls.forEach(
        keyword -> jpaKeywordDTOs.add(JPAKeywordDTO.builder().value(keyword).build()));
    return jpaKeywordDTOs;
  }

  private static Set<JPANormDTO> transformNormsToDTOs(Set<NormXml> normXmls) {
    if (normXmls == null) {
      return null;
    }

    Set<JPANormDTO> jpaNormDTOs = new HashSet<>();
    normXmls.forEach(
        normXml ->
            jpaNormDTOs.add(
                JPANormDTO.builder()
                    .abbreviation(normXml.getAbbreviation())
                    .singleNormDescription(normXml.getSingleNormDescription())
                    .build()));
    return jpaNormDTOs;
  }
}
