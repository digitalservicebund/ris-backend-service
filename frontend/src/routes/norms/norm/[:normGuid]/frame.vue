<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs, ref, watch } from "vue"
import { useRoute } from "vue-router"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import ParticipatingInstitutionInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import {
  FlatMetadata,
  Metadata,
  MetadataSections,
  RangeUnit,
} from "@/domain/Norm"
import { categorizedReference } from "@/fields/norms/categorizedReference"
import { digitalAnnouncement } from "@/fields/norms/digitalAnnouncement"
import { digitalEvidence } from "@/fields/norms/digitalEvidence"
import { documentStatus } from "@/fields/norms/documentStatus"
import { documentTextProof } from "@/fields/norms/documentTextProof"
import { documentType } from "@/fields/norms/documentType"
import { entryIntoForce } from "@/fields/norms/entryIntoForce"
import { euAnnouncement } from "@/fields/norms/euAnnouncement"
import { expiration } from "@/fields/norms/expiration"
import { normProvider } from "@/fields/norms/normProvider"
import { otherDocumentNote } from "@/fields/norms/otherDocumentNote"
import { otherFootnote } from "@/fields/norms/otherFootnote"
import { otherOfficialReferences } from "@/fields/norms/otherOfficialReferences"
import { otherStatusNote } from "@/fields/norms/otherStatusNote"
import { printAnnouncement } from "@/fields/norms/printAnnouncement"
import { reissue } from "@/fields/norms/reissue"
import { repeal } from "@/fields/norms/repeal"
import { status } from "@/fields/norms/status"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import SaveButton from "@/shared/components/input/SaveButton.vue"
import { InputType } from "@/shared/components/input/types"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const metadataSections = computed({
  get: () => loadedNorm.value?.metadataSections ?? {},
  set: (sections: MetadataSections) =>
    loadedNorm.value && (loadedNorm.value.metadataSections = sections),
})

const normSection = computed({
  get: () => metadataSections.value.NORM?.[0] ?? {},
  set: (section: Metadata) => (metadataSections.value.NORM = [section]),
})

const flatMetadata = ref<FlatMetadata>({} as FlatMetadata)

watch(
  loadedNorm,
  () => {
    const { guid, articles, files, metadataSections, ...data } =
      loadedNorm.value ?? {}

    flatMetadata.value = data as FlatMetadata
  },
  { immediate: true, deep: true }
)

watch(
  flatMetadata,
  (data) => {
    if (loadedNorm.value !== undefined && data !== undefined) {
      loadedNorm.value.documentTemplateName =
        data.documentTemplateName as string
      loadedNorm.value.announcementDate = data.announcementDate as string
      loadedNorm.value.applicationScopeArea =
        data.applicationScopeArea as string
      loadedNorm.value.applicationScopeEndDate =
        data.applicationScopeEndDate as string
      loadedNorm.value.applicationScopeStartDate =
        data.applicationScopeStartDate as string
      loadedNorm.value.categorizedReference =
        data.categorizedReference as string
      loadedNorm.value.celexNumber = data.celexNumber as string
      loadedNorm.value.completeCitation = data.completeCitation as string
      loadedNorm.value.digitalAnnouncementDate =
        data.digitalAnnouncementDate as string
      loadedNorm.value.digitalAnnouncementArea =
        data.digitalAnnouncementArea as string
      loadedNorm.value.digitalAnnouncementAreaNumber =
        data.digitalAnnouncementAreaNumber as string
      loadedNorm.value.digitalAnnouncementEdition =
        data.digitalAnnouncementEdition as string
      loadedNorm.value.digitalAnnouncementExplanations =
        data.digitalAnnouncementExplanations as string
      loadedNorm.value.digitalAnnouncementInfo =
        data.digitalAnnouncementInfo as string
      loadedNorm.value.digitalAnnouncementMedium =
        data.digitalAnnouncementMedium as string
      loadedNorm.value.digitalAnnouncementPage =
        data.digitalAnnouncementPage as string
      loadedNorm.value.digitalAnnouncementYear =
        data.digitalAnnouncementYear as string
      loadedNorm.value.digitalEvidenceAppendix =
        data.digitalEvidenceAppendix as string
      loadedNorm.value.digitalEvidenceExternalDataNote =
        data.digitalEvidenceExternalDataNote as string
      loadedNorm.value.digitalEvidenceLink = data.digitalEvidenceLink as string
      loadedNorm.value.digitalEvidenceRelatedData =
        data.digitalEvidenceRelatedData as string
      loadedNorm.value.divergentEntryIntoForceDate =
        data.divergentEntryIntoForceDate as string
      loadedNorm.value.divergentEntryIntoForceDateState =
        data.divergentEntryIntoForceDateState as string
      loadedNorm.value.divergentExpirationDate =
        data.divergentExpirationDate as string
      loadedNorm.value.divergentExpirationDateState =
        data.divergentExpirationDateState as string
      loadedNorm.value.documentCategory = data.documentCategory as string
      loadedNorm.value.documentNormCategory =
        data.documentNormCategory as string
      loadedNorm.value.documentNumber = data.documentNumber as string
      loadedNorm.value.documentStatusDate = data.documentStatusDate as string
      loadedNorm.value.documentStatusDescription =
        data.documentStatusDescription as string
      loadedNorm.value.documentStatusEntryIntoForceDate =
        data.documentStatusEntryIntoForceDate as string
      loadedNorm.value.documentStatusProof = data.documentStatusProof as string
      loadedNorm.value.documentStatusReference =
        data.documentStatusReference as string
      loadedNorm.value.documentStatusWorkNote =
        data.documentStatusWorkNote as string
      loadedNorm.value.documentTextProof = data.documentTextProof as string
      loadedNorm.value.documentTypeName = data.documentTypeName as string
      loadedNorm.value.entryIntoForceDate = data.entryIntoForceDate as string
      loadedNorm.value.entryIntoForceDateState =
        data.entryIntoForceDateState as string
      loadedNorm.value.entryIntoForceNormCategory =
        data.entryIntoForceNormCategory as string
      loadedNorm.value.euAnnouncementExplanations =
        data.euAnnouncementExplanations as string
      loadedNorm.value.euAnnouncementGazette =
        data.euAnnouncementGazette as string
      loadedNorm.value.euAnnouncementInfo = data.euAnnouncementInfo as string
      loadedNorm.value.euAnnouncementNumber =
        data.euAnnouncementNumber as string
      loadedNorm.value.euAnnouncementPage = data.euAnnouncementPage as string
      loadedNorm.value.euAnnouncementSeries =
        data.euAnnouncementSeries as string
      loadedNorm.value.euAnnouncementYear = data.euAnnouncementYear as string
      loadedNorm.value.eli = data.eli as string
      loadedNorm.value.expirationDate = data.expirationDate as string
      loadedNorm.value.expirationDateState = data.expirationDateState as string
      loadedNorm.value.expirationNormCategory =
        data.expirationNormCategory as string
      loadedNorm.value.isExpirationDateTemp =
        data.isExpirationDateTemp as boolean
      loadedNorm.value.officialAbbreviation =
        data.officialAbbreviation as string
      loadedNorm.value.officialLongTitle = data.officialLongTitle as string
      loadedNorm.value.officialShortTitle = data.officialShortTitle as string
      loadedNorm.value.otherDocumentNote = data.otherDocumentNote as string
      loadedNorm.value.otherFootnote = data.otherFootnote as string
      loadedNorm.value.footnoteChange = data.footnoteChange as string
      loadedNorm.value.footnoteComment = data.footnoteComment as string
      loadedNorm.value.footnoteDecision = data.footnoteDecision as string
      loadedNorm.value.footnoteStateLaw = data.footnoteStateLaw as string
      loadedNorm.value.footnoteEuLaw = data.footnoteEuLaw as string
      loadedNorm.value.otherOfficialAnnouncement =
        data.otherOfficialAnnouncement as string
      loadedNorm.value.otherStatusNote = data.otherStatusNote as string
      loadedNorm.value.principleEntryIntoForceDate =
        data.principleEntryIntoForceDate as string
      loadedNorm.value.principleEntryIntoForceDateState =
        data.principleEntryIntoForceDateState as string
      loadedNorm.value.principleExpirationDate =
        data.principleExpirationDate as string
      loadedNorm.value.principleExpirationDateState =
        data.principleExpirationDateState as string
      loadedNorm.value.printAnnouncementExplanations =
        data.printAnnouncementExplanations as string
      loadedNorm.value.printAnnouncementGazette =
        data.printAnnouncementGazette as string
      loadedNorm.value.printAnnouncementInfo =
        data.printAnnouncementInfo as string
      loadedNorm.value.printAnnouncementNumber =
        data.printAnnouncementNumber as string
      loadedNorm.value.printAnnouncementPage =
        data.printAnnouncementPage as string
      loadedNorm.value.printAnnouncementYear =
        data.printAnnouncementYear as string
      loadedNorm.value.providerEntity = data.providerEntity as string
      loadedNorm.value.providerDecidingBody =
        data.providerDecidingBody as string
      loadedNorm.value.providerIsResolutionMajority =
        data.providerIsResolutionMajority as boolean
      loadedNorm.value.publicationDate = data.publicationDate as string
      loadedNorm.value.reissueArticle = data.reissueArticle as string
      loadedNorm.value.reissueDate = data.reissueDate as string
      loadedNorm.value.reissueNote = data.reissueNote as string
      loadedNorm.value.reissueReference = data.reissueReference as string
      loadedNorm.value.repealArticle = data.repealArticle as string
      loadedNorm.value.repealDate = data.repealDate as string
      loadedNorm.value.repealNote = data.repealNote as string
      loadedNorm.value.repealReferences = data.repealReferences as string
      loadedNorm.value.risAbbreviation = data.risAbbreviation as string
      loadedNorm.value.statusDate = data.statusDate as string
      loadedNorm.value.statusDescription = data.statusDescription as string
      loadedNorm.value.statusNote = data.statusNote as string
      loadedNorm.value.statusReference = data.statusReference as string
      loadedNorm.value.text = data.text as string
    }
  },
  { deep: true }
)

function getLabel(value: string, unit: RangeUnit): string {
  const pluralN = value === "1" ? "" : "n"
  switch (unit) {
    case RangeUnit.YEARS:
      return value + ` Jahr` + (value === "1" ? "" : "e")
    case RangeUnit.MONTHS:
      return value + ` Monat` + (value === "1" ? "" : "e")
    case RangeUnit.WEEKS:
      return value + " Woche" + pluralN
    case RangeUnit.DAYS:
      return value + " Tag" + (value === "1" ? "" : "e")
    case RangeUnit.HOURS:
      return value + " Stunde" + pluralN
    case RangeUnit.MINUTES:
      return value + " Minute" + pluralN
    case RangeUnit.SECONDS:
      return value + " Sekunde" + pluralN
    case RangeUnit.YEARS_OF_LIFE:
      return value + " Lebensjahre"
    case RangeUnit.MONTHS_OF_LIFE:
      return value + " Lebensmonate"
  }
}

function ageIndicatorSummarizer(data: Metadata): string {
  if (!data) return ""

  const start = data.RANGE_START?.[0]
  const startUnit = data.RANGE_START_UNIT?.[0]
  const end = data.RANGE_END?.[0]
  const endUnit = data.RANGE_END_UNIT?.[0]

  if (start && startUnit) {
    if (end && endUnit) {
      const startLabel = getLabel(start, startUnit)
      const endLabel = getLabel(end, endUnit)
      return `${startLabel} - ${endLabel}`
    } else {
      const label = getLabel(start, startUnit)
      return `${label}`
    }
  } else if (end && endUnit) {
    const endLabel = getLabel(end, endUnit)
    return ` - ${endLabel}`
  } else {
    return ""
  }
}

function citationDateSummarizer(data: Metadata): string {
  if (!data) return ""

  if (data.YEAR) {
    return data.YEAR.toString()
  }

  function formatDate(dateStrings: string[] | undefined): string {
    if (!dateStrings) {
      return ""
    }

    const dateString = Array.isArray(dateStrings) ? dateStrings[0] : dateStrings

    const date = new Date(dateString)
    const day = date.getDate().toString().padStart(2, "0")
    const month = (date.getMonth() + 1).toString().padStart(2, "0")
    const year = date.getFullYear().toString()
    return `${day}.${month}.${year}`
  }

  return formatDate(data.DATE)
}

const AgeIndicationSummary = withSummarizer(ageIndicatorSummarizer)
const CitationDateSummary = withSummarizer(citationDateSummarizer)
</script>

<template>
  <div class="flex flex-col gap-8 max-w-screen-lg">
    <SingleDataFieldSection
      id="officialLongTitle"
      v-model="flatMetadata.officialLongTitle"
      label="Amtliche Langüberschrift"
    />

    <SingleDataFieldSection
      id="risAbbreviation"
      v-model="flatMetadata.risAbbreviation"
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
      v-model="flatMetadata.documentNumber"
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
      v-model="flatMetadata.documentCategory"
      label="Dokumentart"
    />

    <SingleDataFieldSection
      id="frameKeywords"
      v-model="normSection.KEYWORD"
      label="Schlagwörter im Rahmenelement"
      :type="InputType.CHIPS"
    />

    <h1 class="h-[1px] mt-40 overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>

    <fieldset>
      <legend id="documentTypeFields" class="heading-02-regular mb-[1rem]">
        Dokumenttyp
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentType"
      />
    </fieldset>

    <fieldset class="mb-32">
      <legend id="normProviderFields" class="heading-02-regular mb-[1rem]">
        Normgeber
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="normProvider"
      />
    </fieldset>

    <ExpandableDataSet
      id="participatingInstitutions"
      :data-set="metadataSections.PARTICIPATION"
      title="Mitwirkende Organe"
    >
      <EditableList
        v-model="metadataSections.PARTICIPATION"
        :default-value="{}"
        :edit-component="ParticipatingInstitutionInputGroup"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="leads"
      :data-set="metadataSections.LEAD"
      title="Federführung"
    >
      <EditableList
        v-model="metadataSections.LEAD"
        :default-value="{}"
        :edit-component="LeadInputGroup"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="subjectAreas"
      :data-set="metadataSections.SUBJECT_AREA"
      title="Sachgebiet"
    >
      <EditableList
        v-model="metadataSections.SUBJECT_AREA"
        :default-value="{}"
        :edit-component="SubjectAreaInputGroup"
      />
    </ExpandableDataSet>

    <SingleDataFieldSection
      id="officialShortTitle"
      v-model="flatMetadata.officialShortTitle"
      label="Amtliche Kurzüberschrift"
    />

    <SingleDataFieldSection
      id="officialAbbreviation"
      v-model="flatMetadata.officialAbbreviation"
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

    <fieldset>
      <legend
        id="entryIntoForceFields"
        class="heading-02-regular mb-[1rem] mt-32"
      >
        Inkrafttreten
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="entryIntoForce"
      />
    </fieldset>

    <fieldset>
      <legend id="expirationFields" class="heading-02-regular mb-[1rem]">
        Außerkrafttreten
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="expiration"
      />
    </fieldset>

    <SingleDataFieldSection
      id="announcementDate"
      v-model="flatMetadata.announcementDate"
      label="Verkündungsdatum"
      :type="InputType.DATE"
    />

    <SingleDataFieldSection
      id="publicationDate"
      v-model="flatMetadata.publicationDate"
      label="Veröffentlichungsdatum"
      :type="InputType.DATE"
    />

    <ExpandableDataSet
      id="citationDates"
      :data-set="metadataSections.CITATION_DATE"
      :summary-component="CitationDateSummary"
      title="Zitierdatum"
    >
      <EditableList
        v-model="metadataSections.CITATION_DATE"
        :default-value="{}"
        :edit-component="CitationDateInputGroup"
        :summary-component="CitationDateSummary"
      />
    </ExpandableDataSet>

    <h2
      id="officialAnnouncementFields"
      class="heading-02-regular mb-[1rem] mt-32"
    >
      Amtliche Fundstelle
    </h2>
    <fieldset>
      <legend id="printAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Papierverkündung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="printAnnouncement"
      />
    </fieldset>

    <fieldset>
      <legend
        id="digitalAnnouncementFields"
        class="heading-03-regular mb-[1rem]"
      >
        Elektronisches Verkündungsblatt
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="digitalAnnouncement"
      />
    </fieldset>

    <fieldset>
      <legend id="euAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Amtsblatt der EU
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="euAnnouncement"
      />
    </fieldset>

    <fieldset>
      <legend
        id="otherOfficialReferencesFields"
        class="heading-03-regular mb-[1rem]"
      >
        Sonstige amtliche Fundstelle
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherOfficialReferences"
      />
    </fieldset>

    <SingleDataFieldSection
      id="unofficialReferences"
      v-model="normSection.UNOFFICIAL_REFERENCE"
      label="Nichtamtliche Fundstelle"
      :type="InputType.CHIPS"
    />

    <SingleDataFieldSection
      id="completeCitation"
      v-model="flatMetadata.completeCitation"
      label="Vollzitat"
    />

    <h2 id="statusIndicationFields" class="heading-02-regular mb-[1rem] mt-32">
      Stand-Angabe
    </h2>
    <fieldset>
      <legend id="statusFields" class="heading-03-regular mb-[1rem]">
        Stand
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="status" />
    </fieldset>

    <fieldset>
      <legend id="repealFields" class="heading-03-regular mb-[1rem]">
        Aufhebung
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="repeal" />
    </fieldset>

    <fieldset>
      <legend id="reissueFields" class="heading-03-regular mb-[1rem]">
        Neufassung
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="reissue" />
    </fieldset>

    <fieldset>
      <legend id="otherStatusNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherStatusNote"
      />
    </fieldset>

    <h2
      id="documentProcessingStatusFields"
      class="heading-02-regular mb-[1rem]"
    >
      Stand der dokumentarischen Bearbeitung
    </h2>
    <fieldset>
      <legend id="documentStatusFields" class="heading-03-regular mb-[1rem]">
        Stand der dokumentarischen Bearbeitung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentStatus"
      />
    </fieldset>

    <fieldset>
      <legend id="documentTextProofFields" class="heading-03-regular mb-[1rem]">
        Textnachweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentTextProof"
      />
    </fieldset>

    <fieldset>
      <legend id="otherDocumentNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherDocumentNote"
      />
    </fieldset>

    <fieldset>
      <legend
        id="categorizedReferenceFields"
        class="heading-02-regular mb-[1rem]"
      >
        Aktivverweisung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="categorizedReference"
      />
    </fieldset>

    <fieldset>
      <legend id="otherFootnoteFields" class="heading-02-regular mb-[1rem]">
        Fußnote
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherFootnote"
      />
    </fieldset>

    <SingleDataFieldSection
      id="validityRules"
      v-model="normSection.VALIDITY_RULE"
      label="Gültigkeitsregelung"
      :type="InputType.CHIPS"
    />

    <fieldset class="mt-32">
      <legend id="digitalEvidenceFields" class="heading-02-regular mb-[1rem]">
        Elektronischer Nachweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="digitalEvidence"
      />
    </fieldset>

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
      v-model="flatMetadata.celexNumber"
      label="CELEX-Nummer"
    />

    <ExpandableDataSet
      id="ageIndications"
      :data-set="metadataSections.AGE_INDICATION"
      :summary-component="AgeIndicationSummary"
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

    <SingleDataFieldSection
      id="text"
      v-model="flatMetadata.text"
      label="Text"
    />

    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
