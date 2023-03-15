package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.utils.ServiceUtils.byteBufferToArray;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.SubjectFieldTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtsXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypesXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StatesXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLawXml;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldsOfLawXml;
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

  private final DocumentTypeRepository documentTypeRepository;
  private final JPADocumentTypeRepository jpaDocumentTypeRepository;
  private final CourtRepository courtRepository;
  private final StateRepository stateRepository;
  private final JPAFieldOfLawRepository jpaFieldOfLawRepository;
  private final JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository;

  private static final Pattern FIELD_OF_LAW_NUMBER_PATTERN =
      Pattern.compile("\\p{Lu}{2}(-\\d{2})+(?![\\p{L}\\d-])");

  public LookupTableImporterService(
      DocumentTypeRepository documentTypeRepository,
      JPADocumentTypeRepository jpaDocumentTypeRepository,
      CourtRepository courtRepository,
      StateRepository stateRepository,
      JPAFieldOfLawRepository jpaFieldOfLawRepository,
      JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository) {
    this.documentTypeRepository = documentTypeRepository;
    this.jpaDocumentTypeRepository = jpaDocumentTypeRepository;
    this.courtRepository = courtRepository;
    this.stateRepository = stateRepository;
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

    return courtRepository
        .deleteAll()
        .thenMany(courtRepository.saveAll(courtsDTO))
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

  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<String> importSubjectFieldLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    FieldsOfLawXml fieldsOfLawXml;
    try {
      fieldsOfLawXml = mapper.readValue(byteBufferToArray(byteBuffer), FieldsOfLawXml.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to SubjectFieldXml", e);
    }

    importSubjectFieldJPA(fieldsOfLawXml);

    return Mono.just("Successfully imported the subject field lookup table");
  }

  private void importSubjectFieldJPA(FieldsOfLawXml fieldsOfLawXml) {
    jpaFieldOfLawRepository.deleteAllInBatch();

    List<JPAFieldOfLawDTO> jpaFieldOfLawDTOS =
        fieldsOfLawXml.getList().stream()
            .map(SubjectFieldTransformer::transformToJPADTO)
            .sorted(Comparator.comparing(JPAFieldOfLawDTO::getSubjectFieldNumber))
            .toList();

    setSubjectFieldParentIds(jpaFieldOfLawDTOS);

    jpaFieldOfLawRepository.saveAll(jpaFieldOfLawDTOS);

    extractAndStoreAllLinkedFieldsOfLaw(fieldsOfLawXml);
  }

  private void extractAndStoreAllLinkedFieldsOfLaw(FieldsOfLawXml fieldsOfLawXml) {
    Map<String, Long> allFieldOfLawNumbers =
        fieldsOfLawXml.getList().stream()
            .collect(Collectors.toMap(FieldOfLawXml::getSubjectFieldNumber, FieldOfLawXml::getId));

    List<JPAFieldOfLawLinkDTO> jpaFieldOfLawLinkDTOs = new ArrayList<>();
    fieldsOfLawXml
        .getList()
        .forEach(
            fieldOfLawXml -> {
              for (Long linkedFieldId :
                  extractLinkedFieldsOfLaw(
                      fieldOfLawXml.getSubjectFieldText(), allFieldOfLawNumbers)) {
                jpaFieldOfLawLinkDTOs.add(
                    JPAFieldOfLawLinkDTO.builder()
                        .fieldId(fieldOfLawXml.getId())
                        .linkedFieldId(linkedFieldId)
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

  private void setSubjectFieldParentIds(List<JPAFieldOfLawDTO> jpaFieldOfLawDTOS) {
    Map<String, JPAFieldOfLawDTO> subjectFieldNumberToSubjectFieldDTO =
        jpaFieldOfLawDTOS.stream()
            .collect(
                Collectors.toMap(JPAFieldOfLawDTO::getSubjectFieldNumber, Function.identity()));
    jpaFieldOfLawDTOS.forEach(
        jpaSubjectFieldDTO -> {
          countChildren(jpaSubjectFieldDTO, subjectFieldNumberToSubjectFieldDTO);
          JPAFieldOfLawDTO parentDTO =
              subjectFieldNumberToSubjectFieldDTO.get(
                  jpaSubjectFieldDTO.getSubjectFieldNumberOfParent());
          if (parentDTO != null) {
            jpaSubjectFieldDTO.setParentSubjectField(parentDTO);
          }
        });
  }

  private void countChildren(
      JPAFieldOfLawDTO jpaFieldOfLawDTO,
      Map<String, JPAFieldOfLawDTO> subjectFieldNumberToSubjectFieldDTO) {
    String thisSubjectFieldNumber = jpaFieldOfLawDTO.getSubjectFieldNumber();
    jpaFieldOfLawDTO.setChildrenCount(
        (int)
            subjectFieldNumberToSubjectFieldDTO.keySet().stream()
                .filter(
                    otherSubjectFieldNumber ->
                        otherSubjectFieldNumber.startsWith(thisSubjectFieldNumber)
                            && otherSubjectFieldNumber.length()
                                == thisSubjectFieldNumber.length() + 3)
                .count());
  }
}
