package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ServiceUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldsOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StatesXML;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LookupTableImporterService {
  private final StateRepository stateRepository;
  private final DatabaseCitationTypeRepository databaseCitationTypeRepository;
  private final JPAFieldOfLawRepository jpaFieldOfLawRepository;
  private final JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository;
  private static final Pattern FIELD_OF_LAW_NUMBER_PATTERN =
      Pattern.compile("\\p{Lu}{2}(-\\d{2})+(?![\\p{L}\\d-])");

  public LookupTableImporterService(
      StateRepository stateRepository,
      DatabaseCitationTypeRepository databaseCitationTypeRepository,
      JPAFieldOfLawRepository jpaFieldOfLawRepository,
      JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository) {
    this.stateRepository = stateRepository;
    this.databaseCitationTypeRepository = databaseCitationTypeRepository;
    this.jpaFieldOfLawRepository = jpaFieldOfLawRepository;
    this.jpaFieldOfLawLinkRepository = jpaFieldOfLawLinkRepository;
  }

  public Mono<String> importStateLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    StatesXML statesXML;
    try {
      statesXML = mapper.readValue(ServiceUtils.byteBufferToArray(byteBuffer), StatesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to StatesXML", e);
    }

    List<StateDTO> statesDTO =
        statesXML.getList().stream()
            .map(
                stateXML ->
                    StateDTO.builder()
                        .id(stateXML.getId())
                        .newEntry(true)
                        .changeindicator(stateXML.getChangeIndicator())
                        .version(stateXML.getVersion())
                        .jurisshortcut(stateXML.getJurisShortcut())
                        .label(stateXML.getLabel())
                        .build())
            .toList();

    return stateRepository
        .deleteAll()
        .thenMany(stateRepository.saveAll(statesDTO))
        .collectList()
        .map(list -> "Successfully imported the state lookup table");
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<String> importFieldOfLawLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    FieldsOfLawXml fieldsOfLawXml;
    try {
      fieldsOfLawXml =
          mapper.readValue(ServiceUtils.byteBufferToArray(byteBuffer), FieldsOfLawXml.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to FieldsOfLawXml", e);
    }

    importFieldOfLawJPA(fieldsOfLawXml);

    return Mono.just("Successfully imported the fieldOfLaw lookup table");
  }

  private void importFieldOfLawJPA(FieldsOfLawXml fieldsOfLawXml) {
    jpaFieldOfLawRepository.deleteAllInBatch();

    List<JPAFieldOfLawDTO> jpaFieldOfLawDTOS =
        fieldsOfLawXml.getList().stream()
            .map(FieldOfLawTransformer::transformToJPADTO)
            .sorted(Comparator.comparing(JPAFieldOfLawDTO::getIdentifier))
            .toList();

    setFieldOfLawParentIds(jpaFieldOfLawDTOS);

    jpaFieldOfLawRepository.saveAll(jpaFieldOfLawDTOS);

    extractAndStoreAllLinkedFieldsOfLaw(fieldsOfLawXml);
  }

  private void extractAndStoreAllLinkedFieldsOfLaw(FieldsOfLawXml fieldsOfLawXml) {
    Map<String, Long> allFieldOfLawNumbers =
        fieldsOfLawXml.getList().stream()
            .collect(Collectors.toMap(FieldOfLawXml::getIdentifier, FieldOfLawXml::getId));

    List<JPAFieldOfLawLinkDTO> jpaFieldOfLawLinkDTOs = new ArrayList<>();
    fieldsOfLawXml
        .getList()
        .forEach(
            fieldOfLawXml -> {
              for (Long linkedFieldOfLawId :
                  extractLinkedFieldsOfLaw(fieldOfLawXml.getText(), allFieldOfLawNumbers)) {
                jpaFieldOfLawLinkDTOs.add(
                    JPAFieldOfLawLinkDTO.builder()
                        .fieldOfLawId(fieldOfLawXml.getId())
                        .linkedFieldOfLawId(linkedFieldOfLawId)
                        .build());
              }
            });

    jpaFieldOfLawLinkRepository.saveAll(jpaFieldOfLawLinkDTOs);
  }

  private List<Long> extractLinkedFieldsOfLaw(
      String fieldOfLawText, Map<String, Long> allFieldOfLawNumbers) {
    if (fieldOfLawText == null || fieldOfLawText.isBlank()) {
      return Collections.emptyList();
    }
    List<Long> linkedFieldIds = new ArrayList<>();
    Matcher matcher = FIELD_OF_LAW_NUMBER_PATTERN.matcher(fieldOfLawText);
    while (matcher.find()) {
      String candidateFieldOfLawNumber = matcher.group();
      if (allFieldOfLawNumbers.containsKey(candidateFieldOfLawNumber)) {
        linkedFieldIds.add(allFieldOfLawNumbers.get(candidateFieldOfLawNumber));
      } else {
        log.warn(
            "Found a fieldOfLawNumber in a fieldOfLawText that does not exist in the lookup table: {}",
            candidateFieldOfLawNumber);
      }
    }
    return linkedFieldIds;
  }

  private void setFieldOfLawParentIds(List<JPAFieldOfLawDTO> jpaFieldOfLawDTOS) {
    Map<String, JPAFieldOfLawDTO> identifierToFieldOfLawDTO =
        jpaFieldOfLawDTOS.stream()
            .collect(Collectors.toMap(JPAFieldOfLawDTO::getIdentifier, Function.identity()));
    jpaFieldOfLawDTOS.forEach(
        jpaFieldOfLawDTO -> {
          countChildren(jpaFieldOfLawDTO, identifierToFieldOfLawDTO);
          JPAFieldOfLawDTO parentDTO =
              identifierToFieldOfLawDTO.get(jpaFieldOfLawDTO.getIdentifierOfParent());
          if (parentDTO != null) {
            jpaFieldOfLawDTO.setParentFieldOfLaw(parentDTO);
          }
        });
  }

  private void countChildren(
      JPAFieldOfLawDTO jpaFieldOfLawDTO, Map<String, JPAFieldOfLawDTO> identifierToFieldOfLawDTO) {
    String thisIdentifier = jpaFieldOfLawDTO.getIdentifier();
    jpaFieldOfLawDTO.setChildrenCount(
        (int)
            identifierToFieldOfLawDTO.keySet().stream()
                .filter(
                    otherIdentifier ->
                        otherIdentifier.startsWith(thisIdentifier)
                            && otherIdentifier.length() == thisIdentifier.length() + 3)
                .count());
  }
}
