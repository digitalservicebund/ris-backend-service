<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs, ref, watch } from "vue"
import { useRoute } from "vue-router"
import AgeIndicationInputGroup from "@/components/ageIndication/AgeIndicationInputGroup.vue"
import { ageIndicationSummarizer } from "@/components/ageIndication/summarizer"
import AnnouncementDateInputGroup from "@/components/announcementDate/AnnouncementDateInputGroup.vue"
import { summarizeAnnouncementDate } from "@/components/announcementDate/summarizer"
import CategorizedReferenceInputGroup from "@/components/categorizedReference/CategorizedReferenceInputGroup.vue"
import CitationDateInputGroup from "@/components/citationDate/CitationDateInputGroup.vue"
import DigitalEvidenceInputGroup from "@/components/digitalEvidence/DigitalEvidenceInputGroup.vue"
import { digitalEvidenceSummarizer } from "@/components/digitalEvidence/summarizer"
import DivergentEntryIntoForceGroup from "@/components/divergentGroup/divergentEntryIntoForce/DivergentEntryIntoForceGroup.vue"
import { divergentEntryIntoForceSummarizer } from "@/components/divergentGroup/divergentEntryIntoForce/summarizer"
import DivergentExpirationGroup from "@/components/divergentGroup/divergentExpiration/DivergentExpirationGroup.vue"
import { divergentExpirationSummarizer } from "@/components/divergentGroup/divergentExpiration/summarizer"
import DocumentStatusGroup from "@/components/documentStatus/DocumentStatusGroup.vue"
import { documentStatusSectionSummarizer } from "@/components/documentStatus/summarizer"
import DocumentTypeInputGroup from "@/components/documentType/DocumentTypeInputGroup.vue"
import { documentTypeSummarizer } from "@/components/documentType/summarizer"
import EntryIntoForceInputGroup from "@/components/entryIntoForce/EntryIntoForceInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import ExpirationInputGroup from "@/components/expiration/ExpirationInputGroup.vue"
import FootnoteInput from "@/components/footnote/FootnoteInput.vue"
import { summarizeFootnotePerLine } from "@/components/footnote/summarizer"
import LeadInputGroup from "@/components/lead/LeadInputGroup.vue"
import { leadSummarizer } from "@/components/lead/summarizer"
import NormFrameFooter from "@/components/NormFrameFooter.vue"
import NormProviderInputGroup from "@/components/normProvider/NormProviderInputGroup.vue"
import { normProviderSummarizer } from "@/components/normProvider/summarizer"
import AnnouncementGroup from "@/components/officialReference/AnnouncementGroup.vue"
import { officialReferenceSummarizer } from "@/components/officialReference/summarizer"
import ParticipatingInstitutionInputGroup from "@/components/participatingInstitution/ParticipatingInstitutionInputGroup.vue"
import { participationSummarizer } from "@/components/participatingInstitution/summarizer"
import PrincipleEntryIntoForceInputGroup from "@/components/principleEntryIntoForce/PrincipleEntryIntoForceInputGroup.vue"
import PrincipleExpirationInputGroup from "@/components/principleExpiration/PrincipleExpirationInputGroup.vue"
import PublicationDateInputGroup from "@/components/publicationDate/PublicationDateInputGroup.vue"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import StatusIndicationInputGroup from "@/components/statusIndication/StatusIndicationInputGroup.vue"
import { summarizeStatusIndication } from "@/components/statusIndication/summarizer"
import SubjectAreaInputGroup from "@/components/subjectArea/SubjectAreaInputGroup.vue"
import { subjectAreaSummarizer } from "@/components/subjectArea/summarizer"
import { useLocator } from "@/composables/useLocator"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { FlatMetadata, Metadata, MetadataSections } from "@/domain/norm"
import { dateYearSummarizer } from "@/helpers/dateYearSummarizer"
import { generalSummarizer } from "@/helpers/generalSummarizer"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"
import { InputType } from "@/shared/components/input/types"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const { addSegment } = useLocator()
addSegment(["NORM"])

const metadataSections = computed({
  get: () => loadedNorm.value?.metadataSections ?? {},
  set: (sections: MetadataSections) =>
    loadedNorm.value && (loadedNorm.value.metadataSections = sections),
})

const normSection = computed({
  get: () => metadataSections.value.NORM?.[0] ?? {},
  set: (section: Metadata) => (metadataSections.value.NORM = [section]),
})

const officialLongTitle = computed({
  get: () => normSection.value.OFFICIAL_LONG_TITLE?.[0] ?? "",
  set: (title: string) => (normSection.value.OFFICIAL_LONG_TITLE = [title]),
})

const officialShortTitle = computed({
  get: () => normSection.value.OFFICIAL_SHORT_TITLE?.[0] ?? "",
  set: (title: string) => (normSection.value.OFFICIAL_SHORT_TITLE = [title]),
})

const completeCitation = computed({
  get: () => normSection.value.COMPLETE_CITATION?.[0] ?? "",
  set: (citation: string) => (normSection.value.COMPLETE_CITATION = [citation]),
})

const celexNumber = computed({
  get: () => normSection.value.CELEX_NUMBER?.[0] ?? "",
  set: (number: string) => (normSection.value.CELEX_NUMBER = [number]),
})

const text = computed({
  get: () => normSection.value.TEXT?.[0] ?? "",
  set: (text: string) => (normSection.value.TEXT = [text]),
})

const officialAbbreviation = computed({
  get: () => normSection.value.OFFICIAL_ABBREVIATION?.[0] ?? "",
  set: (abbreviation: string) =>
    (normSection.value.OFFICIAL_ABBREVIATION = [abbreviation]),
})

const risAbbreviation = computed({
  get: () => normSection.value.RIS_ABBREVIATION?.[0] ?? "",
  set: (abbreviation: string) =>
    (normSection.value.RIS_ABBREVIATION = [abbreviation]),
})

const documentNumber = computed({
  get: () => normSection.value.DOCUMENT_NUMBER?.[0] ?? "",
  set: (number: string) => (normSection.value.DOCUMENT_NUMBER = [number]),
})

const documentCategory = computed({
  get: () => normSection.value.DOCUMENT_CATEGORY?.[0] ?? "",
  set: (category: string) => (normSection.value.DOCUMENT_CATEGORY = [category]),
})

const flatMetadata = ref<FlatMetadata>({} as FlatMetadata)

watch(
  loadedNorm,
  (is) => {
    flatMetadata.value = { eli: is?.eli }
  },
  { immediate: true, deep: true },
)

watch(
  flatMetadata,
  (data) => {
    if (loadedNorm.value !== undefined && data !== undefined) {
      loadedNorm.value.eli = data.eli as string
    }
  },
  { deep: true },
)

const DateYearSummary = withSummarizer(dateYearSummarizer)
const OfficialReferenceSummary = withSummarizer(officialReferenceSummarizer)
const DocumentStatusSectionSummary = withSummarizer(
  documentStatusSectionSummarizer,
)
const NormProviderSummary = withSummarizer(normProviderSummarizer)
const DocumentTypeSummary = withSummarizer(documentTypeSummarizer)
const DivergentEntryIntoForceSummary = withSummarizer(
  divergentEntryIntoForceSummarizer,
)
const DivergentExpirationSummary = withSummarizer(divergentExpirationSummarizer)
const GeneralSummary = withSummarizer(generalSummarizer)
const ParticipationSummary = withSummarizer(participationSummarizer)
const LeadSummary = withSummarizer(leadSummarizer)
const SubjectAreaSummary = withSummarizer(subjectAreaSummarizer)
const footnoteLineSummary = withSummarizer(summarizeFootnotePerLine)
const StatusIndicationSummary = withSummarizer(summarizeStatusIndication)
const AnnouncementDateSummary = withSummarizer(summarizeAnnouncementDate)
const DigitalEvidenceSummary = withSummarizer(digitalEvidenceSummarizer)
const AgeIndicationSummary = withSummarizer(ageIndicationSummarizer)
</script>

<template>
  <div class="flex max-w-screen-lg flex-col gap-8">
    <h1 class="h-[1px] w-[1px] overflow-hidden">
      Dokumentation des Rahmenelements
    </h1>

    <SingleDataFieldSection
      id="OFFICIAL_LONG_TITLE"
      v-model="officialLongTitle"
      :input-attributes="{ ariaLabel: 'Amtliche Langüberschrift', rows: 1 }"
      label="Amtliche Langüberschrift"
      required
      :type="InputType.TEXTAREA"
    />

    <SingleDataFieldSection
      id="risAbbreviation"
      v-model="risAbbreviation"
      label="Juris-Abkürzung"
    />

    <SingleDataFieldSection
      id="risAbbreviationInternationalLaw"
      v-model="normSection.RIS_ABBREVIATION_INTERNATIONAL_LAW"
      label="Juris-Abkürzung für völkerrechtliche Vereinbarungen"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="documentNumber"
      v-model="documentNumber"
      label="Dokumentnummer"
    />

    <SingleDataFieldSection
      id="divergentDocumentNumbers"
      v-model="normSection.DIVERGENT_DOCUMENT_NUMBER"
      label="Abweichende Dokumentnummer"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="documentCategory"
      v-model="documentCategory"
      label="Dokumentart"
    />

    <SingleDataFieldSection
      id="frameKeywords"
      v-model="normSection.KEYWORD"
      label="Schlagwörter im Rahmenelement"
      :type="InputType.CHIPS"
    />

    <ExpandableDataSet
      id="documentTypes"
      border-bottom
      :data-set="metadataSections.DOCUMENT_TYPE"
      :summary-component="DocumentTypeSummary"
      test-id="a11y-expandable-dataset"
      title="Dokumenttyp"
    >
      <EditableList
        v-model="metadataSections.DOCUMENT_TYPE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="DocumentTypeInputGroup"
        :summary-component="DocumentTypeSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="normProviders"
      border-bottom
      :data-set="metadataSections.NORM_PROVIDER"
      :summary-component="NormProviderSummary"
      test-id="a11y-expandable-dataset"
      title="Normgeber"
    >
      <EditableList
        v-model="metadataSections.NORM_PROVIDER"
        :default-value="{}"
        :edit-component="NormProviderInputGroup"
        :summary-component="NormProviderSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="participatingInstitutions"
      border-bottom
      :data-set="metadataSections.PARTICIPATION"
      :summary-component="ParticipationSummary"
      test-id="a11y-expandable-dataset"
      title="Mitwirkende Organe"
    >
      <EditableList
        v-model="metadataSections.PARTICIPATION"
        :default-value="{}"
        :edit-component="ParticipatingInstitutionInputGroup"
        :summary-component="ParticipationSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="leads"
      border-bottom
      :data-set="metadataSections.LEAD"
      :summary-component="LeadSummary"
      test-id="a11y-expandable-dataset"
      title="Federführung"
    >
      <EditableList
        v-model="metadataSections.LEAD"
        :default-value="{}"
        :edit-component="LeadInputGroup"
        :summary-component="LeadSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="subjectAreas"
      border-bottom
      :data-set="metadataSections.SUBJECT_AREA"
      :summary-component="SubjectAreaSummary"
      test-id="a11y-expandable-dataset"
      title="Sachgebiet"
    >
      <EditableList
        v-model="metadataSections.SUBJECT_AREA"
        :default-value="{}"
        :edit-component="SubjectAreaInputGroup"
        :summary-component="SubjectAreaSummary"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="officialShortTitle"
      v-model="officialShortTitle"
      label="Amtliche Kurzüberschrift"
    />

    <SingleDataFieldSection
      id="officialAbbreviation"
      v-model="officialAbbreviation"
      label="Amtliche Buchstabenabkürzung"
    />

    <SingleDataFieldSection
      id="unofficialLongTitles"
      v-model="normSection.UNOFFICIAL_LONG_TITLE"
      label="Nichtamtliche Langüberschrift"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="unofficialShortTitles"
      v-model="normSection.UNOFFICIAL_SHORT_TITLE"
      label="Nichtamtliche Kurzüberschrift"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="unofficialAbbreviations"
      v-model="normSection.UNOFFICIAL_ABBREVIATION"
      label="Nichtamtliche Buchstabenabkürzung"
      :type="InputType.CHIPS"
    />

    <ExpandableDataSet
      id="entryIntoForces"
      border-bottom
      :data-set="metadataSections.ENTRY_INTO_FORCE"
      :summary-component="GeneralSummary"
      title="Datum des Inkrafttretens"
    >
      <EditableList
        v-model="metadataSections.ENTRY_INTO_FORCE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="EntryIntoForceInputGroup"
        :summary-component="GeneralSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="principleEntryIntoForces"
      border-bottom
      :data-set="metadataSections.PRINCIPLE_ENTRY_INTO_FORCE"
      :summary-component="GeneralSummary"
      test-id="a11y-expandable-dataset"
      title="Grundsätzliches Inkrafttretedatum"
    >
      <EditableList
        v-model="metadataSections.PRINCIPLE_ENTRY_INTO_FORCE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="PrincipleEntryIntoForceInputGroup"
        :summary-component="GeneralSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="divergentEntryIntoForces"
      border-bottom
      :data-set="metadataSections.DIVERGENT_ENTRY_INTO_FORCE"
      :summary-component="DivergentEntryIntoForceSummary"
      test-id="a11y-expandable-dataset"
      title="Abweichendes Inkrafttretedatum"
    >
      <EditableList
        v-model="metadataSections.DIVERGENT_ENTRY_INTO_FORCE"
        :default-value="{}"
        :edit-component="DivergentEntryIntoForceGroup"
        :summary-component="DivergentEntryIntoForceSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="expirations"
      border-bottom
      :data-set="metadataSections.EXPIRATION"
      :summary-component="GeneralSummary"
      test-id="a11y-expandable-dataset"
      title="Datum des Außerkrafttretens"
    >
      <EditableList
        v-model="metadataSections.EXPIRATION"
        :default-value="{}"
        disable-multi-entry
        :edit-component="ExpirationInputGroup"
        :summary-component="GeneralSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="principleExpirations"
      border-bottom
      :data-set="metadataSections.PRINCIPLE_EXPIRATION"
      :summary-component="GeneralSummary"
      test-id="a11y-expandable-dataset"
      title="Grundsätzliches Außerkrafttretedatum"
    >
      <EditableList
        v-model="metadataSections.PRINCIPLE_EXPIRATION"
        :default-value="{}"
        disable-multi-entry
        :edit-component="PrincipleExpirationInputGroup"
        :summary-component="GeneralSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="divergentExpirations"
      border-bottom
      :data-set="metadataSections.DIVERGENT_EXPIRATION"
      :summary-component="DivergentExpirationSummary"
      test-id="a11y-expandable-dataset"
      title="Abweichendes Außerkrafttretedatum"
    >
      <EditableList
        v-model="metadataSections.DIVERGENT_EXPIRATION"
        :default-value="{}"
        :edit-component="DivergentExpirationGroup"
        :summary-component="DivergentExpirationSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="announcementDate"
      border-bottom
      :data-set="metadataSections.ANNOUNCEMENT_DATE"
      :summary-component="AnnouncementDateSummary"
      test-id="a11y-expandable-dataset"
      title="Verkündungsdatum"
    >
      <EditableList
        v-model="metadataSections.ANNOUNCEMENT_DATE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="AnnouncementDateInputGroup"
        :summary-component="AnnouncementDateSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="publicationDates"
      border-bottom
      :data-set="metadataSections.PUBLICATION_DATE"
      :summary-component="DateYearSummary"
      test-id="a11y-expandable-dataset"
      title="Veröffentlichungsdatum"
    >
      <EditableList
        v-model="metadataSections.PUBLICATION_DATE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="PublicationDateInputGroup"
        :summary-component="DateYearSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="citationDates"
      border-bottom
      :data-set="metadataSections.CITATION_DATE"
      :summary-component="DateYearSummary"
      test-id="a11y-expandable-dataset"
      title="Zitierdatum"
    >
      <EditableList
        v-model="metadataSections.CITATION_DATE"
        :default-value="{}"
        :edit-component="CitationDateInputGroup"
        :summary-component="DateYearSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="officialReferences"
      border-bottom
      :data-set="metadataSections.OFFICIAL_REFERENCE"
      :summary-component="OfficialReferenceSummary"
      test-id="a11y-expandable-dataset"
      title="Amtliche Fundstelle"
    >
      <EditableList
        v-model="metadataSections.OFFICIAL_REFERENCE"
        :default-value="{}"
        :edit-component="AnnouncementGroup"
        :summary-component="OfficialReferenceSummary"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="unofficialReferences"
      v-model="normSection.UNOFFICIAL_REFERENCE"
      label="Nichtamtliche Fundstelle"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="completeCitation"
      v-model="completeCitation"
      label="Vollzitat"
    />

    <ExpandableDataSet
      id="statusIndication"
      border-bottom
      :data-set="metadataSections.STATUS_INDICATION"
      :summary-component="StatusIndicationSummary"
      test-id="a11y-expandable-dataset"
      title="Stand-Angabe"
    >
      <EditableList
        v-model="metadataSections.STATUS_INDICATION"
        :default-value="{}"
        :edit-component="StatusIndicationInputGroup"
        :summary-component="StatusIndicationSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="documentStatus"
      border-bottom
      :data-set="metadataSections.DOCUMENT_STATUS_SECTION"
      :summary-component="DocumentStatusSectionSummary"
      test-id="a11y-expandable-dataset"
      title="Stand der dokumentarischen Bearbeitung"
    >
      <EditableList
        v-model="metadataSections.DOCUMENT_STATUS_SECTION"
        :default-value="{}"
        :edit-component="DocumentStatusGroup"
        :summary-component="DocumentStatusSectionSummary"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="categorizedReferences"
      border-bottom
      :data-set="metadataSections.CATEGORIZED_REFERENCE"
      test-id="a11y-expandable-dataset"
      title="Aktivverweisung"
    >
      <EditableList
        v-model="metadataSections.CATEGORIZED_REFERENCE"
        :default-value="{}"
        :edit-component="CategorizedReferenceInputGroup"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="footnotes"
      border-bottom
      :data-set="metadataSections.FOOTNOTES"
      :summary-component="footnoteLineSummary"
      title="Fußnoten"
    >
      <EditableList
        v-model="metadataSections.FOOTNOTES"
        :default-value="{}"
        :edit-component="FootnoteInput"
        :summary-component="footnoteLineSummary"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="validityRules"
      v-model="normSection.VALIDITY_RULE"
      label="Gültigkeitsregelung"
      :type="InputType.CHIPS"
    />

    <ExpandableDataSet
      id="digitalEvidence"
      border-bottom
      :data-set="metadataSections.DIGITAL_EVIDENCE"
      :summary-component="DigitalEvidenceSummary"
      test-id="a11y-expandable-dataset"
      title="Elektronischer Nachweis"
    >
      <EditableList
        v-model="metadataSections.DIGITAL_EVIDENCE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="DigitalEvidenceInputGroup"
        :summary-component="DigitalEvidenceSummary"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="referenceNumbers"
      v-model="normSection.REFERENCE_NUMBER"
      label="Aktenzeichen"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="eli"
      v-model="flatMetadata.eli"
      label="ELI"
      readonly
    />

    <SingleDataFieldSection
      id="celexNumber"
      v-model="celexNumber"
      label="CELEX-Nummer"
    />

    <ExpandableDataSet
      id="ageIndications"
      border-bottom
      :data-set="metadataSections.AGE_INDICATION"
      :summary-component="AgeIndicationSummary"
      test-id="a11y-expandable-dataset"
      title="Altersangabe"
    >
      <EditableList
        v-model="metadataSections.AGE_INDICATION"
        :default-value="{}"
        :edit-component="AgeIndicationInputGroup"
        :summary-component="AgeIndicationSummary"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="definitions"
      v-model="normSection.DEFINITION"
      label="Definition"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="ageOfMajorityIndications"
      v-model="normSection.AGE_OF_MAJORITY_INDICATION"
      label="Angaben zur Volljährigkeit"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection id="text" v-model="text" label="Text" />
    <NormFrameFooter />
  </div>
</template>
