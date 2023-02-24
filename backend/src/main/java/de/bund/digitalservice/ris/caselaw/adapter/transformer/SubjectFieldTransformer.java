package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.NormXml;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectFieldTransformer {
  private SubjectFieldTransformer() {}

  public static FieldOfLaw transformToDomain(SubjectFieldDTO subjectFieldDTO) {
    List<Keyword> keywords = null;
    if (subjectFieldDTO.getKeywords() != null) {
      keywords =
          subjectFieldDTO.getKeywords().stream()
              .map(keywordDTO -> Keyword.builder().value(keywordDTO.getValue()).build())
              .toList();
    }

    List<Norm> norms = null;
    if (subjectFieldDTO.getNorms() != null) {
      norms =
          subjectFieldDTO.getNorms().stream()
              .map(
                  normDTO ->
                      Norm.builder()
                          .abbreviation(normDTO.getAbbreviation())
                          .singleNormDescription(normDTO.getSingleNormDescription())
                          .build())
              .toList();
    }

    List<String> linkedFields = null;
    if (subjectFieldDTO.getLinkedFields() != null) {
      linkedFields =
          subjectFieldDTO.getLinkedFields().stream()
              .map(SubjectFieldDTO::getSubjectFieldNumber)
              .toList();
    }

    return FieldOfLaw.builder()
        .id(subjectFieldDTO.getId())
        .depth(subjectFieldDTO.getDepthInTree())
        .isLeaf(subjectFieldDTO.isLeafInTree())
        .subjectFieldNumber(subjectFieldDTO.getSubjectFieldNumber())
        .subjectFieldText(subjectFieldDTO.getSubjectFieldText())
        .linkedFields(linkedFields)
        .navigationTerm(subjectFieldDTO.getNavigationTerm())
        .keywords(keywords)
        .norms(norms)
        .children(new ArrayList<>())
        .build();
  }

  public static JPASubjectFieldDTO transformToJPADTO(FieldOfLawXml fieldOfLawXml) {
    return JPASubjectFieldDTO.builder()
        .id(fieldOfLawXml.getId())
        .changeDateMail(fieldOfLawXml.getChangeDateMail())
        .changeDateClient(fieldOfLawXml.getChangeDateClient())
        .changeIndicator(fieldOfLawXml.getChangeIndicator())
        .version(fieldOfLawXml.getVersion())
        .subjectFieldNumber(fieldOfLawXml.getSubjectFieldNumber())
        .subjectFieldText(fieldOfLawXml.getSubjectFieldText())
        .navigationTerm(fieldOfLawXml.getNavigationTerm())
        .keywords(transformKeywordsToJPADTOs(fieldOfLawXml.getKeywords()))
        .norms(transformNormsToJPADTOs(fieldOfLawXml.getNorms()))
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
