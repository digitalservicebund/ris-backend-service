<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs, ref, watch, h } from "vue"
import { useRoute } from "vue-router"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import AnnouncementGroup from "@/components/AnnouncementGroup.vue"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import NormProviderInputGroup from "@/components/NormProviderInputGroup.vue"
import ParticipatingInstitutionInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { FlatMetadata, Metadata, MetadataSections } from "@/domain/Norm"
import { categorizedReference } from "@/fields/norms/categorizedReference"
import { digitalEvidence } from "@/fields/norms/digitalEvidence"
import { documentStatus } from "@/fields/norms/documentStatus"
import { documentTextProof } from "@/fields/norms/documentTextProof"
import { documentType } from "@/fields/norms/documentType"
import { entryIntoForce } from "@/fields/norms/entryIntoForce"
import { expiration } from "@/fields/norms/expiration"
import { otherDocumentNote } from "@/fields/norms/otherDocumentNote"
import { otherFootnote } from "@/fields/norms/otherFootnote"
import { otherStatusNote } from "@/fields/norms/otherStatusNote"
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

function printAnnouncementSummary(data: Metadata): string {
  if (!data || !data.length) return ""
  const printAnnouncementData = data[0] // assuming there's only one item in the array
  if (!printAnnouncementData) return ""
  // generate summary string based on the printAnnouncementData
  return "Print Announcement Summary"
}
function digitalAnnouncementSummary(data: Metadata): string {
  if (!data || !data.length) return ""
  const digitalAnnouncementData = data[0] // assuming there's only one item in the array
  if (!digitalAnnouncementData) return ""
  // generate summary string based on the digitalAnnouncementData
  return "Digital Announcement Summary"
}
function euAnnouncementSummary(data: Metadata): string {
  if (!data || !data.length) return ""
  const euAnnouncementData = data[0] // assuming there's only one item in the array
  if (!euAnnouncementData) return ""
  // generate summary string based on the euAnnouncementData
  return "EU Announcement Summary"
}
function otherOfficialReferenceSummary(data: Metadata): string {
  if (!data || !data.length) return ""
  const otherOfficialReferenceData = data[0] // assuming there's only one item in the array
  if (!otherOfficialReferenceData) return ""
  // generate summary string based on the otherOfficialReferenceData
  return "Other Official Reference Summary"
}
function officialReferenceSummarizer(data: MetadataSections): string {
  // if (data.PRINT_ANNOUNCEMENT && data.PRINT_ANNOUNCEMENT.length > 0) {
  //   return printAnnouncementSummary(data.PRINT_ANNOUNCEMENT);
  // } else if (data.DIGITAL_ANNOUNCEMENT && data.DIGITAL_ANNOUNCEMENT.length > 0) {
  //   return digitalAnnouncementSummary(data.DIGITAL_ANNOUNCEMENT);
  // } else if (data.EU_ANNOUNCEMENT && data.EU_ANNOUNCEMENT.length > 0) {
  //   return euAnnouncementSummary(data.EU_ANNOUNCEMENT);
  // } else if (data.OTHER_OFFICIAL_ANNOUNCEMENT && data.OTHER_OFFICIAL_ANNOUNCEMENT.length > 0) {
  //   return otherOfficialReferenceSummary(data.OTHER_OFFICIAL_ANNOUNCEMENT);
  // }
  // return '';
  if (!data) return "no data"
  if (data.PRINT_ANNOUNCEMENT) {
    return printAnnouncementSummary(data.PRINT_ANNOUNCEMENT[0])
  } else if (data.DIGITAL_ANNOUNCEMENT) {
    return digitalAnnouncementSummary(data.DIGITAL_ANNOUNCEMENT[0])
  } else if (data.EU_ANNOUNCEMENT) {
    return euAnnouncementSummary(data.EU_ANNOUNCEMENT[0])
  } else if (data.OTHER_OFFICIAL_ANNOUNCEMENT) {
    return otherOfficialReferenceSummary(data.OTHER_OFFICIAL_ANNOUNCEMENT[0])
  } else return ""
}

function normProviderSummarizer(data: Metadata) {
  if (!data) return ""

  const entity = data.ENTITY?.[0]
  const decidingBody = data.DECIDING_BODY?.[0]
  const isResolutionMajority = data.RESOLUTION_MAJORITY?.[0]

  const summaryLine = [entity, decidingBody]
    .filter((value) => value != "" && value != null)
    .join(" | ")

  if (isResolutionMajority) {
    return h("div", { class: ["flex", "gap-8"] }, [
      h(
        "span",
        summaryLine.length == 0 ? summaryLine : summaryLine.concat(" | ")
      ),
      h("img", {
        src: CheckMark,
        width: "16",
        alt: "Schwarzes Haken",
      }),
      h("span", "Beschlussfassung mit qual. Mehrheit"),
    ])
  } else {
    return summaryLine
  }
}

const CitationDateSummary = withSummarizer(citationDateSummarizer)
const OfficialReferenceSummary = withSummarizer(officialReferenceSummarizer)
const NormProviderSummary = withSummarizer(normProviderSummarizer)
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

    <ExpandableDataSet
      id="normProviders"
      :data-set="metadataSections.NORM_PROVIDER"
      :summary-component="NormProviderSummary"
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

    <ExpandableDataSet
      id="officialAnnouncementFields"
      :data-set="metadataSections.OFFICIAL_REFERENCE"
      :summary-component="OfficialReferenceSummary"
      title="Amtliche Fundstelle"
    >
      <EditableList
        v-model="metadataSections.PRINT_ANNOUNCEMENT"
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
      title="Altersangabe"
    >
      <EditableList
        v-model="metadataSections.AGE_INDICATION"
        :default-value="{}"
        :edit-component="AgeIndicationInputGroup"
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
