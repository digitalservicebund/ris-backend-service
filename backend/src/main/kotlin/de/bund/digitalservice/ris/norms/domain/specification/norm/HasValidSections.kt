package de.bund.digitalservice.ris.norms.domain.specification.norm

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.AGE_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.ANNOUNCEMENT_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.CATEGORIZED_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.CITATION_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIGITAL_EVIDENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DOCUMENT_STATUS_SECTION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DOCUMENT_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.FOOTNOTES
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.LEAD
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.NORM
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.NORM_PROVIDER
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PARTICIPATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINCIPLE_EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PUBLICATION_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.STATUS_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.SUBJECT_AREA

val hasValidSections =
    object : Specification<Norm> {
        override fun isSatisfiedBy(instance: Norm): Boolean = instance.metadataSections.all {
            it.name in listOf(
                NORM,
                SUBJECT_AREA,
                LEAD,
                PARTICIPATION,
                CITATION_DATE,
                AGE_INDICATION,
                OFFICIAL_REFERENCE,
                NORM_PROVIDER,
                DOCUMENT_TYPE,
                DIVERGENT_ENTRY_INTO_FORCE,
                DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
                DIVERGENT_EXPIRATION,
                DIVERGENT_EXPIRATION_UNDEFINED,
                CATEGORIZED_REFERENCE,
                ENTRY_INTO_FORCE,
                PRINCIPLE_ENTRY_INTO_FORCE,
                EXPIRATION,
                PRINCIPLE_EXPIRATION,
                DIGITAL_EVIDENCE,
                FOOTNOTES,
                DOCUMENT_STATUS_SECTION,
                STATUS_INDICATION,
                PUBLICATION_DATE,
                ANNOUNCEMENT_DATE,
            )
        }
    }
