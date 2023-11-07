package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "xml_publication")
@Entity
public class XmlPublicationDTO {
  @Id Long id;

  @Column("document_unit_id")
  UUID documentUnitId;

  @Column("receiver_address")
  String receiverAddress;

  @Column("mail_subject")
  String mailSubject;

  String xml;

  @Column("status_code")
  String statusCode;

  @Column("status_messages")
  String statusMessages;

  @Column("file_name")
  String fileName;

  @Column("publish_date")
  Instant publishDate;
}
