package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.lang.reflect.Member;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.uuid.UuidGenerator;

/**
 * Use a manually assigned id or create a new UUID if no value is set.
 *
 * <p>This Generator uses the {@link UuidGenerator} but changes the handling if an id is already
 * assigned. The normal UuidGenerator throws a {@link org.hibernate.StaleObjectStateException} in
 * such a case. This generator uses the assigned id.
 *
 * <p>We need this to do a migration when splitting one table into two using a rolling updates
 * strategy. For this we needed to write into two tables at the same time and use the same id for
 * both. Therefore, we need to set the id manually. To keep the code we need to modify for this
 * minimal we couldn't remove the {@link jakarta.persistence.GeneratedValue} annotation. This class
 * allows us to still use generated values for most cases and just set them manually when needed.
 */
public class AssignedIdOrUuidGenerator extends UuidGenerator {
  public AssignedIdOrUuidGenerator(
      AssignedIdOrUuid config, Member member, GeneratorCreationContext creationContext) {
    super(null, member, creationContext);
  }

  @Override
  public boolean allowAssignedIdentifiers() {
    return true;
  }

  @Override
  public Object generate(
      SharedSessionContractImplementor session,
      Object owner,
      Object currentValue,
      EventType eventType) {
    if (currentValue == null) {
      return super.generate(session, owner, currentValue, eventType);
    }

    return currentValue;
  }
}
