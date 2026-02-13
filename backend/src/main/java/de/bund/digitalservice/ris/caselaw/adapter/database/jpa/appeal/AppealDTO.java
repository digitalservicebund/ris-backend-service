package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealWithdrawal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.PkhPlaintiff;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(name = "appeal")
public class AppealDTO {

  @Id
  @Column(name = "decision_id")
  private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "decision_id")
  private DecisionDTO decision;

  /** Rechtsmittelführer */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealAppellantDTO> appellants;

  /** Revision (Beklagter) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealRevisionDefendantDTO> revisionDefendantStatuses;

  /** Revision (Kläger) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealRevisionPlaintiffDTO> revisionPlaintiffStatuses;

  /** Anschlussrevision (Beklagter) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealJointRevisionDefendantDTO> jointRevisionDefendantStatuses;

  /** Anschlussrevision (Kläger) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealJointRevisionPlaintiffDTO> jointRevisionPlaintiffStatuses;

  /** NZB (Beklagter) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealNzbDefendantDTO> nzbDefendantStatuses;

  /** NZB (Kläger) */
  @OneToMany(
      mappedBy = "appeal",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Valid
  @OrderBy("rank")
  private List<AppealNzbPlaintiffDTO> nzbPlaintiffStatuses;

  /** Zurücknahme der Revision */
  @Column(name = "appeal_withdrawal")
  @Enumerated(EnumType.STRING)
  private AppealWithdrawal appealWithdrawal;

  /** PKH-Antrag (Kläger) */
  @Column(name = "pkh_plaintiff")
  @Enumerated(EnumType.STRING)
  private PkhPlaintiff pkhPlaintiff;

  @Override
  @SuppressWarnings("java:S2097") // Class type check is not recognized by Sonar
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    AppealDTO that = (AppealDTO) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
