package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
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
@Builder(toBuilder = true)
@Entity
@Table(schema = "incremental_migration", name = "appeal_appellant")
public class AppealAppellantDTO {

  @EmbeddedId @Builder.Default private AppealAppellantId primaryKey = new AppealAppellantId();

  @ManyToOne
  @MapsId("appealId")
  @JoinColumn(name = "appeal_id")
  private AppealDTO appeal;

  @ManyToOne
  @MapsId("appellantId")
  @JoinColumn(name = "appellant_id")
  private AppellantDTO appellant;

  private int rank;

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
    AppealAppellantDTO that = (AppealAppellantDTO) o;
    return getPrimaryKey() != null && Objects.equals(getPrimaryKey(), that.getPrimaryKey());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
