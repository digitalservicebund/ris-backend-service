package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtsXML;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypesXML;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LookupTableImporterService {

  private final DocumentTypeRepository documentTypeRepository;
  private final CourtRepository courtRepository;

  public LookupTableImporterService(
      DocumentTypeRepository documentTypeRepository, CourtRepository courtRepository) {
    this.documentTypeRepository = documentTypeRepository;
    this.courtRepository = courtRepository;
  }

  public Mono<String> importDocumentTypeLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    DocumentTypesXML documentTypesXML;
    try {
      documentTypesXML = mapper.readValue(byteBuffer.array(), DocumentTypesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    documentTypeRepository
        .deleteAll()
        .doOnNext(
            unused ->
                documentTypesXML
                    .getList()
                    .forEach(
                        documentTypeXML ->
                            documentTypeRepository
                                .save(
                                    DocumentTypeDTO.builder()
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
                                .subscribe()));

    return Mono.just("Successfully imported the document type lookup table");
  }

  public Mono<String> importCourtLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    CourtsXML courtsXML;
    try {
      courtsXML = mapper.readValue(byteBuffer.array(), CourtsXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    courtRepository
        .deleteAll()
        .doOnNext(
            unused ->
                courtsXML
                    .getList()
                    .forEach(
                        courtXML ->
                            courtRepository
                                .save(
                                    CourtDTO.builder()
                                        .id(courtXML.getId())
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
                                        .currentofficialcourtname(
                                            courtXML.getCurrentofficialcourtname())
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
                                .subscribe()));

    return Mono.just("Successfully imported the court lookup table");
  }
}
