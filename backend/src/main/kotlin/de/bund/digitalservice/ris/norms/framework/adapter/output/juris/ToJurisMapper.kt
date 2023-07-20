package de.bund.digitalservice.ris.norms.framework.adapter.output.juris

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.ProofType
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateToGermanFormat
import de.bund.digitalservice.ris.norms.juris.converter.model.CategorizedReference
import de.bund.digitalservice.ris.norms.juris.converter.model.DigitalAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentStatus
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.Footnote
import de.bund.digitalservice.ris.norms.juris.converter.model.Lead
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.Participation
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.Reissue
import de.bund.digitalservice.ris.norms.juris.converter.model.Status
import de.bund.digitalservice.ris.norms.juris.converter.model.SubjectArea
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm as NormData

fun mapDomainToData(norm: Norm): NormData {
    return NormData(
        officialLongTitle = extractSimpleStringValuesFromNormSection(norm, MetadatumType.OFFICIAL_LONG_TITLE).firstOrNull(),
        risAbbreviation = extractSimpleStringValuesFromNormSection(norm, MetadatumType.RIS_ABBREVIATION).firstOrNull(),
        risAbbreviationInternationalLawList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW),
        unofficialLongTitleList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.UNOFFICIAL_LONG_TITLE),
        unofficialShortTitleList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.UNOFFICIAL_SHORT_TITLE),
        unofficialAbbreviationList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.UNOFFICIAL_ABBREVIATION),
        divergentDocumentNumber = extractFirstStringValue(norm, Section.NORM, MetadatumType.DIVERGENT_DOCUMENT_NUMBER),
        documentCategory = extractSimpleStringValuesFromNormSection(norm, MetadatumType.DOCUMENT_CATEGORY).firstOrNull(),
        participationList = extractParticipations(norm),
        leadList = extractLeads(norm),
        subjectAreaList = extractSubjectAreas(norm),
        frameKeywordList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.KEYWORD),
        normProviderList = extractNormProviders(norm),
        officialShortTitle = extractSimpleStringValuesFromNormSection(norm, MetadatumType.OFFICIAL_SHORT_TITLE).firstOrNull(),
        officialAbbreviation = extractSimpleStringValuesFromNormSection(norm, MetadatumType.OFFICIAL_ABBREVIATION).firstOrNull(),
        announcementDate = extractAnnouncementDate(norm),
        citationDateList = extractCitationDates(norm),
        printAnnouncementList = extractPrintAnnouncementList(norm),
        digitalAnnouncementList = extractDigitalAnnouncementList(norm),
        categorizedReferences = extractCategorizedReferences(norm),
        referenceNumberList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.REFERENCE_NUMBER),
        definitionList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.DEFINITION),
        validityRuleList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.VALIDITY_RULE),
        documentType = extractDocumentType(norm),
        divergentEntryIntoForceList = extractDivergentEntryIntoForces(norm),
        divergentExpirationsList = extractDivergentExpirations(norm),
        ageOfMajorityIndicationList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.AGE_OF_MAJORITY_INDICATION),
        ageIndicationStartList = extractAgeIndicationStarts(norm),
        celexNumber = extractSimpleStringValuesFromNormSection(norm, MetadatumType.CELEX_NUMBER).firstOrNull(),
        text = extractSimpleStringValuesFromNormSection(norm, MetadatumType.TEXT).firstOrNull(),
        entryIntoForceDate = extractEntryIntoForceDate(norm),
        entryIntoForceDateState = extractEntryIntoForceState(norm),
        expirationDate = extractExpirationDate(norm),
        expirationDateState = extractExpirationState(norm),
        unofficialReferenceList = extractSimpleStringValuesFromNormSection(norm, MetadatumType.UNOFFICIAL_REFERENCE),
        statusList = extractStatus(norm),
        reissueList = extractReissue(norm),
        repealList = extractRepealStatus(norm),
        otherStatusList = extractOtherStatus(norm),
        footnotes = extractFootnotes(norm),
        documentTextProof = extractDocumentTextProof(norm),
        documentStatus = extractDocumentStatus(norm),
    )
}

private fun extractDocumentStatus(norm: Norm): List<DocumentStatus> = norm
    .metadataSections
    .filter { it.name == Section.DOCUMENT_STATUS_SECTION }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.DOCUMENT_STATUS }
    .map { section ->
        val date = section.metadata.find { it.type == MetadatumType.DATE }?.let { found -> encodeLocalDate(found.value as LocalDate) }
        val year = section.metadata.find { it.type == MetadatumType.YEAR }?.let { it.value.toString() }
        DocumentStatus(
            section.metadata.filter { it.type == MetadatumType.WORK_NOTE }.map { it.value.toString() },
            section.metadata.find { it.type == MetadatumType.DESCRIPTION }?.let { it.value.toString() },
            date ?: year,
            section.metadata.find { it.type == MetadatumType.REFERENCE }?.let { it.value.toString() },
        )
    }
private fun extractDocumentTextProof(norm: Norm): String? {
    val metadata = norm.metadataSections
        .filter { it.name == Section.DOCUMENT_STATUS_SECTION }
        .flatMap { it.sections ?: listOf() }
        .find { it.name == Section.DOCUMENT_TEXT_PROOF }?.metadata
    val proofType = metadata?.find { it.type == MetadatumType.PROOF_TYPE }?.value
    val proofText = metadata?.find { it.type == MetadatumType.TEXT }?.value
    return if (proofType != null && proofText != null) {
        ProofType.valueOf(proofType.toString()).text + " " + proofText
    } else {
        null
    }
}

private fun extractFootnotes(norm: Norm): List<Footnote> {
    fun transformFootnotes(section: MetadataSection, type: MetadatumType): List<Pair<Int, String>> {
        return section.metadata.filter { metadata -> metadata.type == type }.map { metadata ->
            Pair(metadata.order, metadata.value.toString())
        }
    }
    return norm
        .metadataSections
        .filter { section -> section.name == Section.FOOTNOTES }
        .map { footnoteSection ->
            val reference = footnoteSection.metadata.find { metadata -> metadata.type == MetadatumType.FOOTNOTE_REFERENCE }?.let { it.value.toString() }
            Footnote(
                reference,
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_CHANGE),
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_COMMENT),
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_DECISION),
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_STATE_LAW),
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_EU_LAW),
                transformFootnotes(footnoteSection, MetadatumType.FOOTNOTE_OTHER),
            )
        }
}

private fun extractAnnouncementDate(norm: Norm): String? {
    val section = norm.metadataSections.firstOrNull { it.name == Section.ANNOUNCEMENT_DATE }

    val year = section?.metadata?.firstOrNull { it.type == MetadatumType.YEAR }
    val yearValue = year?.value?.toString()

    val date = section?.metadata?.firstOrNull { it.type == MetadatumType.DATE }
    val dateValue = encodeLocalDate(date?.value as LocalDate?)

    val time = section?.metadata?.firstOrNull { it.type == MetadatumType.TIME }
    val timeValue = encodeLocalTime(time?.value as LocalTime?)

    return when {
        year != null -> yearValue
        date != null && time == null -> dateValue
        date != null && time != null -> "$dateValue $timeValue"
        else -> null
    }
}

fun extractCitationDates(norm: Norm): List<String> =
    norm.metadataSections
        .filter { it.name == Section.CITATION_DATE }
        .sortedBy { it.order }
        .flatMap { it.metadata }
        .map {
            if (it.type == MetadatumType.DATE) {
                encodeLocalDate(it.value as LocalDate)
            } else {
                it.value.toString()
            }
        }
        .filterNotNull()

private fun extractSimpleStringValuesFromNormSection(norm: Norm, metadatumType: MetadatumType): List<String> {
    return norm.metadataSections
        .filter { it.name == Section.NORM }
        .flatMap { it.metadata }
        .filter { it.type == metadatumType }
        .sortedBy { it.order }
        .map { it.value.toString() }
}

private fun extractFirstStringValue(norm: Norm, sectionName: MetadataSectionName, metadatumType: MetadatumType): String {
    return norm.metadataSections
        .filter { it.name == sectionName }
        .flatMap { it.metadata }
        .filter { it.type == metadatumType }
        .minByOrNull { it.order }?.value.toString()
}

private fun extractCategorizedReferences(norm: Norm): List<CategorizedReference> = norm
    .metadataSections
    .filter { it.name == Section.CATEGORIZED_REFERENCE }
    .map { section ->
        CategorizedReference(
            section.metadata.find { it.type == MetadatumType.TEXT }?.value.toString(),
        )
    }
private fun extractStatus(norm: Norm): List<Status> = norm
    .metadataSections
    .filter { it.name == Section.STATUS_INDICATION }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.STATUS }
    .map { section ->
        Status(
            section.metadata.find { it.type == MetadatumType.NOTE }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.DESCRIPTION }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.DATE }?.let { found -> encodeLocalDateToGermanFormat(found.value as LocalDate) },
            section.metadata.filter { it.type == MetadatumType.REFERENCE }.joinToString { it.value.toString() },
        )
    }

private fun extractReissue(norm: Norm): List<Reissue> = norm
    .metadataSections
    .filter { it.name == Section.STATUS_INDICATION }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.REISSUE }
    .map { section ->
        Reissue(
            section.metadata.find { it.type == MetadatumType.NOTE }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.ARTICLE }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.DATE }?.let { found -> encodeLocalDateToGermanFormat(found.value as LocalDate) },
            section.metadata.find { it.type == MetadatumType.REFERENCE }?.value.toString(),
        )
    }

private fun extractRepealStatus(norm: Norm): List<String> = norm
    .metadataSections
    .filter { it.name == Section.STATUS_INDICATION }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.REPEAL }
    .flatMap { it.metadata }
    .map { metadatum -> metadatum.value.toString() }

private fun extractOtherStatus(norm: Norm): List<String> = norm
    .metadataSections
    .filter { it.name == Section.STATUS_INDICATION }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.OTHER_STATUS }
    .flatMap { it.metadata }
    .map { metadatum -> metadatum.value.toString() }

private fun extractNormProviders(norm: Norm): List<NormProvider> = norm
    .metadataSections
    .filter { section -> section.name == Section.NORM_PROVIDER }
    .map {
        val entity = it.metadata.find { metadatum -> metadatum.type == MetadatumType.ENTITY }?.let { found -> found.value.toString() }
        val decidingBody = it.metadata.find { metadatum -> metadatum.type == MetadatumType.DECIDING_BODY }?.let { found -> found.value.toString() }
        val isResolutionMajority = it.metadata.find { metadatum -> metadatum.type == MetadatumType.RESOLUTION_MAJORITY }?.let { found -> found.value.toString().toBoolean() }
        NormProvider(entity, decidingBody, isResolutionMajority)
    }

private fun extractParticipations(norm: Norm): List<Participation> = norm
    .metadataSections
    .filter { section -> section.name == Section.PARTICIPATION }
    .map {
        val institution = it.metadata.find { metadatum -> metadatum.type == MetadatumType.PARTICIPATION_INSTITUTION }?.let { found -> found.value.toString() }
        val type = it.metadata.find { metadatum -> metadatum.type == MetadatumType.PARTICIPATION_TYPE }?.let { found -> found.value.toString() }
        Participation(type, institution)
    }

private fun extractLeads(norm: Norm): List<Lead> = norm
    .metadataSections
    .filter { section -> section.name == Section.LEAD }
    .map {
        val jurisdiction = it.metadata.find { metadatum -> metadatum.type == MetadatumType.LEAD_JURISDICTION }?.let { found -> found.value.toString() }
        val unit = it.metadata.find { metadatum -> metadatum.type == MetadatumType.LEAD_UNIT }?.let { found -> found.value.toString() }
        Lead(jurisdiction, unit)
    }

private fun extractSubjectAreas(norm: Norm): List<SubjectArea> {
    val subjectAreaList = mutableListOf<SubjectArea>()
    norm.metadataSections
        .filter { section -> section.name == Section.SUBJECT_AREA }
        .map {
            it.metadata.forEach { metadatum ->
                if (metadatum.type == MetadatumType.SUBJECT_FNA) {
                    subjectAreaList.add(SubjectArea(fna = metadatum.value as String))
                }
                if (metadatum.type == MetadatumType.SUBJECT_GESTA) {
                    subjectAreaList.add(SubjectArea(gesta = metadatum.value as String))
                }
            }
        }
    return subjectAreaList
}

private fun extractDivergentEntryIntoForces(norm: Norm): List<DivergentEntryIntoForce> = norm
    .metadataSections
    .filter { section -> section.name == Section.DIVERGENT_ENTRY_INTO_FORCE }
    .mapNotNull { it.sections }.flatten()
    .map {
        val date = it.metadata.find { metadatum -> metadatum.type == MetadatumType.DATE }?.let { found -> encodeLocalDate(found.value as LocalDate) }
        val state = it.metadata.find { metadatum -> metadatum.type == MetadatumType.UNDEFINED_DATE }?.let { found -> found.value.toString() }
        val normCategory = it.metadata.firstOrNull { metadatum -> metadatum.type == MetadatumType.NORM_CATEGORY }?.let { found -> parseDomainNormToData(NormCategory.valueOf(found.value.toString())) }
        DivergentEntryIntoForce(date, state, normCategory)
    }

private fun extractDivergentExpirations(norm: Norm): List<DivergentExpiration> = norm
    .metadataSections
    .filter { section -> section.name == Section.DIVERGENT_EXPIRATION }
    .mapNotNull { it.sections }.flatten()
    .map {
        val date = it.metadata.find { metadatum -> metadatum.type == MetadatumType.DATE }?.let { found -> encodeLocalDate(found.value as LocalDate) }
        val state = it.metadata.find { metadatum -> metadatum.type == MetadatumType.UNDEFINED_DATE }?.let { found -> found.value.toString() }
        val normCategory = it.metadata.firstOrNull { metadatum -> metadatum.type == MetadatumType.NORM_CATEGORY }?.let { found -> parseDomainNormToData(NormCategory.valueOf(found.value.toString())) }
        DivergentExpiration(date, state, normCategory)
    }

private fun extractDocumentType(norm: Norm): DocumentType? = norm
    .metadataSections
    .filter { it.name == Section.DOCUMENT_TYPE }
    .map { section ->
        DocumentType(
            section.metadata.find { it.type == MetadatumType.TYPE_NAME }?.value.toString(),
            section.metadata.filter { it.type == MetadatumType.TEMPLATE_NAME }.map { it.value.toString() },
            section.metadata.filter { it.type == MetadatumType.NORM_CATEGORY }.mapNotNull { parseDomainNormToData(NormCategory.valueOf(it.value.toString())) },
        )
    }.firstOrNull()

private fun extractPrintAnnouncementList(norm: Norm): List<PrintAnnouncement> = norm
    .metadataSections
    .filter { it.name == Section.OFFICIAL_REFERENCE }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.PRINT_ANNOUNCEMENT }
    .map { section ->
        PrintAnnouncement(
            section.metadata.find { it.type == MetadatumType.YEAR }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.PAGE }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.ANNOUNCEMENT_GAZETTE }?.value.toString(),
        )
    }

private fun extractDigitalAnnouncementList(norm: Norm): List<DigitalAnnouncement> = norm
    .metadataSections
    .filter { it.name == Section.OFFICIAL_REFERENCE }
    .flatMap { it.sections ?: listOf() }
    .filter { it.name == Section.DIGITAL_ANNOUNCEMENT }
    .map { section ->
        DigitalAnnouncement(
            section.metadata.find { it.type == MetadatumType.YEAR }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.EDITION }?.value.toString(),
            section.metadata.find { it.type == MetadatumType.ANNOUNCEMENT_MEDIUM }?.value.toString(),
        )
    }

private fun extractAgeIndicationStarts(norm: Norm): List<String> = norm
    .metadataSections
    .filter { section -> section.name == Section.AGE_INDICATION }
    .mapNotNull {
        it.metadata.find { metadatum -> metadatum.type == MetadatumType.RANGE_START }?.let { found -> found.value.toString() }
    }

private fun extractEntryIntoForceDate(norm: Norm): String? = norm
    .metadataSections
    .firstOrNull { section -> section.name == Section.ENTRY_INTO_FORCE }
    ?.let { it.metadata.find { metadatum -> metadatum.type == MetadatumType.DATE }?.let { found -> encodeLocalDate(found.value as LocalDate) } }

private fun extractEntryIntoForceState(norm: Norm): String? = norm
    .metadataSections
    .firstOrNull { section -> section.name == Section.ENTRY_INTO_FORCE }
    ?.let { it.metadata.find { metadatum -> metadatum.type == MetadatumType.UNDEFINED_DATE }?.let { found -> found.value.toString() } }

private fun extractExpirationDate(norm: Norm): String? = norm
    .metadataSections
    .firstOrNull { section -> section.name == Section.EXPIRATION }
    ?.let { it.metadata.find { metadatum -> metadatum.type == MetadatumType.DATE }?.let { found -> encodeLocalDate(found.value as LocalDate) } }

private fun extractExpirationState(norm: Norm): String? = norm
    .metadataSections
    .firstOrNull { section -> section.name == Section.EXPIRATION }
    ?.let { it.metadata.find { metadatum -> metadatum.type == MetadatumType.UNDEFINED_DATE }?.let { found -> found.value.toString() } }

private fun parseDomainNormToData(normCategory: NormCategory?): String? = when (normCategory) {
    NormCategory.BASE_NORM -> "SN"
    NormCategory.AMENDMENT_NORM -> "ÄN"
    NormCategory.TRANSITIONAL_NORM -> "ÜN"
    else -> null
}

private fun encodeLocalTime(time: LocalTime?) = time?.let { it.format(DateTimeFormatter.ofPattern("HH:mm")) }
