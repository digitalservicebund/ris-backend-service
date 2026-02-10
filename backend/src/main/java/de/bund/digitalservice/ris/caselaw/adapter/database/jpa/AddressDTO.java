package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "address")
public class AddressDTO {
  @Id @GeneratedValue private UUID id;

  @Column private String street;

  @Column(name = "postal_code")
  private String postalCode;

  @Column private String city;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "fax_number")
  private String faxNumber;

  @Column private String email;

  @Column private String url;

  @Column(name = "post_office_box")
  private String postOfficeBox;

  @Column(name = "post_office_box_postal_code")
  private String postOfficeBoxPostalCode;

  @Column(name = "post_office_box_location")
  private String postOfficeBoxLocation;
}
