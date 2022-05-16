package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.repository.DocUnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocUnitService {
    private final DocUnitRepository repository;

    public DocUnitService(DocUnitRepository repository) {
        Assert.notNull(repository, "doc unit repository is null");

        this.repository = repository;
    }

    public Mono<ResponseEntity<DocUnit>> generateNewDocUnit(FilePart filePart) {
        log.debug("generate doc unit for {}", filePart.filename());
        var docUnitEntity = generateDataObject(filePart.filename(), "docx");

        log.debug("save doc unit");
        var docUnit = repository.save(docUnitEntity);

        return docUnit.map(ResponseEntity::ok);
    }

    private DocUnit generateDataObject(String filename, String type) {
        var docUnit = new DocUnit();
        docUnit.setS3path(filename);
        docUnit.setFiletype(type);
        return docUnit;
    }
}
