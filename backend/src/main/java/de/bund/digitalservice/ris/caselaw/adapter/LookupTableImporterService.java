package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.ServiceUtils.byteBufferToArray;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationsStyleXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtsXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypesXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldsOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StatesXML;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  private final DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  private final JPADocumentTypeRepository jpaDocumentTypeRepository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final StateRepository stateRepository;
  private final CitationStyleRepository citationStyleRepository;
  private final JPAFieldOfLawRepository jpaFieldOfLawRepository;
  private final JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository;
  private static final Pattern FIELD_OF_LAW_NUMBER_PATTERN =
      Pattern.compile("\\p{Lu}{2}(-\\d{2})+(?![\\p{L}\\d-])");

  public LookupTableImporterService(
      DatabaseDocumentTypeRepository databaseDocumentTypeRepository,
      JPADocumentTypeRepository jpaDocumentTypeRepository,
      DatabaseCourtRepository databaseCourtRepository,
      StateRepository stateRepository,
      CitationStyleRepository citationStyleRepository,
      JPAFieldOfLawRepository jpaFieldOfLawRepository,
      JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository) {
    this.databaseDocumentTypeRepository = databaseDocumentTypeRepository;
    this.jpaDocumentTypeRepository = jpaDocumentTypeRepository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.stateRepository = stateRepository;
    this.citationStyleRepository = citationStyleRepository;
    this.jpaFieldOfLawRepository = jpaFieldOfLawRepository;
    this.jpaFieldOfLawLinkRepository = jpaFieldOfLawLinkRepository;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<String> importDocumentTypeLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    DocumentTypesXML documentTypesXML;
    try {
      documentTypesXML = mapper.readValue(byteBufferToArray(byteBuffer), DocumentTypesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    importDocumentTypeJPA(documentTypesXML);

    //    List<DocumentTypeDTO> documentTypeDTOs =
    //        documentTypesXML.getList().stream()
    //            .map(
    //                documentTypeXML ->
    //                    DocumentTypeDTO.builder()
    //                        .id(documentTypeXML.getId())
    //                        .changeDateClient(documentTypeXML.getChangeDateClient())
    //                        .changeIndicator(documentTypeXML.getChangeIndicator())
    //                        .version(documentTypeXML.getVersion())
    //                        .jurisShortcut(documentTypeXML.getJurisShortcut())
    //                        .documentType(documentTypeXML.getDocumentType())
    //                        .multiple(documentTypeXML.getMultiple())
    //                        .label(documentTypeXML.getLabel())
    //                        .superlabel1(documentTypeXML.getSuperlabel1())
    //                        .superlabel2(documentTypeXML.getSuperlabel2())
    //                        .build())
    //            .toList();
    //
    //    documentTypeRepository
    //        .deleteAll()
    //        .thenMany(documentTypeRepository.saveAll(documentTypeDTOs))
    //        .subscribe();

    return Mono.just("Successfully imported the document type lookup table");
  }

  public void importDocumentTypeJPA(DocumentTypesXML documentTypesXML) {
    List<JPADocumentTypeDTO> documentTypeDTOS =
        documentTypesXML.getList().stream()
            .map(
                documentTypeXML ->
                    JPADocumentTypeDTO.builder()
                        .id(documentTypeXML.getId())
                        .changeDateClient(documentTypeXML.getChangeDateClient())
                        .changeIndicator(documentTypeXML.getChangeIndicator())
                        .version(documentTypeXML.getVersion())
                        .jurisShortcut(documentTypeXML.getJurisShortcut())
                        .documentType(documentTypeXML.getDocumentType())
                        .multiple(documentTypeXML.getMultiple())
                        .label(documentTypeXML.getLabel())
                        .superlabel1(documentTypeXML.getSuperlabel1())
                        .superlabel2(documentTypeXML.getSuperlabel2())
                        .build())
            .toList();

    jpaDocumentTypeRepository.saveAll(documentTypeDTOS);
  }

  public Mono<String> importCourtLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    CourtsXML courtsXML;
    try {
      courtsXML = mapper.readValue(byteBufferToArray(byteBuffer), CourtsXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to CourtsXML", e);
    }

    List<CourtDTO> courtsDTO =
        courtsXML.getList().stream()
            .map(
                courtXML ->
                    CourtDTO.builder()
                        .id(courtXML.getId())
                        .newEntry(true)
                        .changedatemail(courtXML.getChangeDateMail())
                        .changedateclient(courtXML.getChangeDateClient())
                        .changeindicator(courtXML.getChangeIndicator())
                        .version(courtXML.getVersion())
                        .courttype(courtXML.getCourtType())
                        .courtlocation(courtXML.getCourtLocation())
                        .field(courtXML.getField())
                        .superiorcourt(courtXML.getSuperiorcourt())
                        .foreigncountry(courtXML.getForeignCountry())
                        .region(courtXML.getRegion())
                        .federalstate(courtXML.getFederalState())
                        .belongsto(courtXML.getBelongsto())
                        .street(courtXML.getStreet())
                        .zipcode(courtXML.getZipcode())
                        .maillocation(courtXML.getMaillocation())
                        .phone(courtXML.getPhone())
                        .fax(courtXML.getFax())
                        .postofficebox(courtXML.getPostofficebox())
                        .postofficeboxzipcode(courtXML.getPostofficeboxzipcode())
                        .postofficeboxlocation(courtXML.getPostofficeboxlocation())
                        .email(courtXML.getEmail())
                        .internet(courtXML.getInternet())
                        .isbranchofficeto(courtXML.getIsbranchofficeto())
                        .earlycourtname(courtXML.getEarlycourtname())
                        .latecourtname(courtXML.getLatecourtname())
                        .currentofficialcourtname(courtXML.getCurrentofficialcourtname())
                        .traditionalcourtname(courtXML.getTraditionalcourtname())
                        .existingbranchoffice(courtXML.getExistingbranchoffice())
                        .abandonedbranchoffice(courtXML.getAbandonedbranchoffice())
                        .contactperson(courtXML.getContactperson())
                        .deliverslrs(courtXML.getDeliverslrs())
                        .remark(courtXML.getRemark())
                        .additional(courtXML.getAdditional())
                        .existencedate(courtXML.getExistencedate())
                        .cancellationdate(courtXML.getCancellationdate())
                        .build())
            .toList();

    return databaseCourtRepository
        .deleteAll()
        .thenMany(databaseCourtRepository.saveAll(courtsDTO))
        .collectList()
        .map(list -> "Successfully imported the court lookup table");
  }

  public Mono<String> importStateLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    StatesXML statesXML;
    try {
      statesXML = mapper.readValue(byteBufferToArray(byteBuffer), StatesXML.class);
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

  public Mono<String> importCitationStyleLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    CitationsStyleXML citationsStyleXML;
    try {
      citationsStyleXML = mapper.readValue(byteBufferToArray(byteBuffer), CitationsStyleXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to CitationsXML", e);
    }

    List<CitationStyleDTO> citationsDTO =
        citationsStyleXML.getList().stream()
            .map(
                citationXML ->
                    CitationStyleDTO.builder()
                        .jurisId(citationXML.getId())
                        .newEntry(true)
                        .uuid(UUID.randomUUID())
                        .changeIndicator(citationXML.getChangeIndicator())
                        .changeDateMail(
                            citationXML.getChangeDateMail() != null
                                ? LocalDate.parse(citationXML.getChangeDateMail())
                                : null)
                        .version(citationXML.getVersion())
                        .documentType(citationXML.getDocumentType())
                        .citationDocumentType(citationXML.getCitationDocumentType())
                        .jurisShortcut(citationXML.getJurisShortcut())
                        .label(citationXML.getLabel())
                        .build())
            .toList();

    return citationStyleRepository
        .deleteAll()
        .thenMany(citationStyleRepository.saveAll(citationsDTO))
        .then(Mono.just("Successfully imported the state lookup table"));
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<String> importFieldOfLawLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    FieldsOfLawXml fieldsOfLawXml;
    try {
      fieldsOfLawXml = mapper.readValue(byteBufferToArray(byteBuffer), FieldsOfLawXml.class);
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
