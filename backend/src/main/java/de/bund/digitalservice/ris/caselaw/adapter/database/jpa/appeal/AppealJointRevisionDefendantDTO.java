package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(schema = "incremental_migration", name = "appeal_joint_revision_defendant")
public class AppealJointRevisionDefendantDTO extends AppealAppealStatusDTO {}
