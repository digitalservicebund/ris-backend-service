package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "xml_publication")
@Entity
public class XmlPublicationDTO {
  @Id @GeneratedValue UUID id;

  @Column(name = "documentation_unit_id")
  UUID documentUnitId;

  @Column(name = "receiver_address")
  String receiverAddress;

  @Column(name = "mail_subject")
  String mailSubject;

  String xml;

  @Column(name = "status_code")
  String statusCode;

  @Column(name = "status_messages")
  String statusMessages;

  @Column(name = "file_name")
  String fileName;

  @Column(name = "publish_date")
  Instant publishDate;

  @Column(name = "issuer_address")
  private String issuerAddress;
}
