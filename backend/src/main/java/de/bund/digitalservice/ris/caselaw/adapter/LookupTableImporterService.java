package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.utils.ServiceUtils.byteBufferToArray;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.SubjectFieldTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtsXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypesXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StatesXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectFieldsXml;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
  private final JPASubjectFieldRepository jpaSubjectFieldRepository;

  public LookupTableImporterService(
      DocumentTypeRepository documentTypeRepository,
      JPADocumentTypeRepository jpaDocumentTypeRepository,
      CourtRepository courtRepository,
      StateRepository stateRepository,
      JPASubjectFieldRepository jpaSubjectFieldRepository) {
    this.documentTypeRepository = documentTypeRepository;
    this.jpaDocumentTypeRepository = jpaDocumentTypeRepository;
    this.courtRepository = courtRepository;
    this.stateRepository = stateRepository;
    this.jpaSubjectFieldRepository = jpaSubjectFieldRepository;
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
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
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
    SubjectFieldsXml subjectFieldsXml;
    try {
      subjectFieldsXml = mapper.readValue(byteBufferToArray(byteBuffer), SubjectFieldsXml.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to SubjectFieldXml", e);
    }

    importSubjectFieldJPA(subjectFieldsXml);

    return Mono.just("Successfully imported the subject field lookup table");
  }

  private void importSubjectFieldJPA(SubjectFieldsXml subjectFieldsXml) {
    jpaSubjectFieldRepository.deleteAllInBatch();

    List<JPASubjectFieldDTO> jpaSubjectFieldDTOs =
        subjectFieldsXml.getList().stream()
            .map(SubjectFieldTransformer::transformToJPADTO)
            .sorted(Comparator.comparing(JPASubjectFieldDTO::getSubjectFieldNumber))
            .toList();

    setSubjectFieldParentIds(jpaSubjectFieldDTOs);
    setSubjectFieldParent(jpaSubjectFieldDTOs); // is the parent boolean necessary?

    jpaSubjectFieldRepository.saveAll(jpaSubjectFieldDTOs);
  }

  private void setSubjectFieldParentIds(List<JPASubjectFieldDTO> jpaSubjectFieldDTOs) {
    Map<String, JPASubjectFieldDTO> subjectFieldNumberToSubjectFieldDTO =
        jpaSubjectFieldDTOs.stream()
            .collect(
                Collectors.toMap(JPASubjectFieldDTO::getSubjectFieldNumber, Function.identity()));
    jpaSubjectFieldDTOs.forEach(
        jpaSubjectFieldDTO -> {
          // leaves
          String thisSubjectFieldNumber = jpaSubjectFieldDTO.getSubjectFieldNumber();
          jpaSubjectFieldDTO.setLeafInTree(
              subjectFieldNumberToSubjectFieldDTO.keySet().stream()
                  .noneMatch(
                      otherSubjectFieldNumber ->
                          otherSubjectFieldNumber.startsWith(thisSubjectFieldNumber)
                              && !otherSubjectFieldNumber.equals(thisSubjectFieldNumber)));
          // depth
          jpaSubjectFieldDTO.setDepthInTree(
              jpaSubjectFieldDTO.getSubjectFieldNumber().split("-").length);
          JPASubjectFieldDTO parentDTO =
              subjectFieldNumberToSubjectFieldDTO.get(
                  jpaSubjectFieldDTO.getSubjectFieldNumberOfParent());
          if (parentDTO != null) {
            jpaSubjectFieldDTO.setParentSubjectField(parentDTO);
          }
        });
  }

  private void setSubjectFieldParent(List<JPASubjectFieldDTO> jpaSubjectFieldDTOs) {
    Set<Long> parentIds = getSubjectFieldParentIds(jpaSubjectFieldDTOs);
    jpaSubjectFieldDTOs.stream()
        .filter(jpaSubjectFieldDTO -> parentIds.contains(jpaSubjectFieldDTO.getId()))
        .forEach(jpaSubjectFieldDTO -> jpaSubjectFieldDTO.setParent(true));
  }

  private Set<Long> getSubjectFieldParentIds(List<JPASubjectFieldDTO> jpaSubjectFieldDTOs) {
    return jpaSubjectFieldDTOs.stream()
        .filter(jpaSubjectFieldDTO -> jpaSubjectFieldDTO.getParentSubjectField() != null)
        .map(jpaSubjectFieldDTO -> jpaSubjectFieldDTO.getParentSubjectField().getId())
        .collect(Collectors.toSet());
  }
}
