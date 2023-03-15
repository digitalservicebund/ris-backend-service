package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.NormXml;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FieldOfLawTransformer {
  private FieldOfLawTransformer() {}

  public static FieldOfLaw transformToDomain(FieldOfLawDTO fieldOfLawDTO) {
    List<Keyword> keywords = null;
    if (fieldOfLawDTO.getKeywords() != null) {
      keywords =
          fieldOfLawDTO.getKeywords().stream()
              .map(keywordDTO -> Keyword.builder().value(keywordDTO.getValue()).build())
              .toList();
    }

    List<Norm> norms = null;
    if (fieldOfLawDTO.getNorms() != null) {
      norms =
          fieldOfLawDTO.getNorms().stream()
              .map(
                  normDTO ->
                      Norm.builder()
                          .abbreviation(normDTO.getAbbreviation())
                          .singleNormDescription(normDTO.getSingleNormDescription())
                          .build())
              .toList();
    }

    List<String> linkedFields = null;
    if (fieldOfLawDTO.getLinkedFieldsOfLaw() != null) {
      linkedFields =
          fieldOfLawDTO.getLinkedFieldsOfLaw().stream().map(FieldOfLawDTO::getIdentifier).toList();
    }

    return FieldOfLaw.builder()
        .id(fieldOfLawDTO.getId())
        .childrenCount(fieldOfLawDTO.getChildrenCount())
        .identifier(fieldOfLawDTO.getIdentifier())
        .text(fieldOfLawDTO.getText())
        .linkedFields(linkedFields)
        .keywords(keywords)
        .norms(norms)
        .children(new ArrayList<>())
        .build();
  }

  public static JPAFieldOfLawDTO transformToJPADTO(FieldOfLawXml fieldOfLawXml) {
    return JPAFieldOfLawDTO.builder()
        .id(fieldOfLawXml.getId())
        .changeDateMail(fieldOfLawXml.getChangeDateMail())
        .changeDateClient(fieldOfLawXml.getChangeDateClient())
        .changeIndicator(fieldOfLawXml.getChangeIndicator())
        .version(fieldOfLawXml.getVersion())
        .identifier(fieldOfLawXml.getIdentifier())
        .text(fieldOfLawXml.getText())
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
