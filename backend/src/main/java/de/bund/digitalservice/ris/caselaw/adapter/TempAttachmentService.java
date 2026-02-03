package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TempAttachmentService {
  private final AttachmentRepository attachmentRepository;

  public TempAttachmentService(AttachmentRepository attachmentRepository) {
    this.attachmentRepository = attachmentRepository;
  }

  @Transactional
  public void saveAttachment(AttachmentDTO attachmentDTO) {
    attachmentRepository.save(attachmentDTO);
  }
}
