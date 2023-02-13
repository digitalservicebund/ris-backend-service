package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.NormXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectFieldXml;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectFieldTransformer {

  public static SubjectField transformToDomain(SubjectFieldDTO subjectFieldDTO) {
    List<Keyword> keywords =
        subjectFieldDTO.getKeywords().stream()
            .map(keywordDTO -> Keyword.builder().value(keywordDTO.getValue()).build())
            .toList();
    List<Norm> norms =
        subjectFieldDTO.getNorms().stream()
            .map(
                normDTO ->
                    Norm.builder()
                        .abbreviation(normDTO.getAbbreviation())
                        .singleNormDescription(normDTO.getSingleNormDescription())
                        .build())
            .toList();
    return SubjectField.builder()
        .id(subjectFieldDTO.getId())
        .subjectFieldNumber(subjectFieldDTO.getSubjectFieldNumber())
        .subjectFieldText(subjectFieldDTO.getSubjectFieldText())
        .navigationTerm(subjectFieldDTO.getNavigationTerm())
        .keywords(keywords)
        .norms(norms)
        .build();
  }

  public static JPASubjectFieldDTO transformToJPADTO(SubjectFieldXml subjectFieldXml) {
    return JPASubjectFieldDTO.builder()
        .id(subjectFieldXml.getId())
        .changeDateMail(subjectFieldXml.getChangeDateMail())
        .changeDateClient(subjectFieldXml.getChangeDateClient())
        .changeIndicator(subjectFieldXml.getChangeIndicator())
        .version(subjectFieldXml.getVersion())
        .subjectFieldNumber(subjectFieldXml.getSubjectFieldNumber())
        .subjectFieldText(subjectFieldXml.getSubjectFieldText())
        .navigationTerm(subjectFieldXml.getNavigationTerm())
        .keywords(transformKeywordsToJPADTOs(subjectFieldXml.getKeywords()))
        .norms(transformNormsToJPADTOs(subjectFieldXml.getNorms()))
        .build();
  }

  private static Set<JPAKeywordDTO> transformKeywordsToJPADTOs(Set<String> keywordXmls) {
    if (keywordXmls == null) {
      return null;
    }

    Set<JPAKeywordDTO> jpaKeywordDTOs = new HashSet<>();
    keywordXmls.forEach(
        keyword -> jpaKeywordDTOs.add(JPAKeywordDTO.builder().value(keyword).build()));
    return jpaKeywordDTOs;
  }

  private static Set<JPANormDTO> transformNormsToJPADTOs(Set<NormXml> normXmls) {
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
