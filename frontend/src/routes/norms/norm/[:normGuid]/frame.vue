<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs, ref, watch, h, VNode, createTextVNode } from "vue"
import { useRoute } from "vue-router"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import AnnouncementGroup from "@/components/AnnouncementGroup.vue"
import CategorizedReferenceInputGroup from "@/components/CategorizedReferenceInputGroup.vue"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"
import DivergentEntryIntoForceGroup from "@/components/DivergentEntryIntoForceGroup.vue"
import DivergentExpirationGroup from "@/components/DivergentExpirationGroup.vue"
import DocumentTypeInputGroup from "@/components/DocumentTypeInputGroup.vue"
import EntryIntoForceInputGroup from "@/components/EntryIntoForceInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import ExpirationInputGroup from "@/components/ExpirationInputGroup.vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import NormProviderInputGroup from "@/components/NormProviderInputGroup.vue"
import ParticipatingInstitutionInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import PrincipleEntryIntoForceInputGroup from "@/components/PrincipleEntryIntoForceInputGroup.vue"
import PrincipleExpirationInputGroup from "@/components/PrincipleExpirationInputGroup.vue"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import {
  FlatMetadata,
  Metadata,
  MetadataSections,
  UndefinedDate,
} from "@/domain/Norm"
import { digitalEvidence } from "@/fields/norms/digitalEvidence"
import { documentStatus } from "@/fields/norms/documentStatus"
import { documentTextProof } from "@/fields/norms/documentTextProof"
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
      loadedNorm.value.applicationScopeArea =
        data.applicationScopeArea as string
      loadedNorm.value.applicationScopeEndDate =
        data.applicationScopeEndDate as string
      loadedNorm.value.applicationScopeStartDate =
        data.applicationScopeStartDate as string
      loadedNorm.value.celexNumber = data.celexNumber as string
      loadedNorm.value.completeCitation = data.completeCitation as string
      loadedNorm.value.digitalEvidenceAppendix =
        data.digitalEvidenceAppendix as string
      loadedNorm.value.digitalEvidenceExternalDataNote =
        data.digitalEvidenceExternalDataNote as string
      loadedNorm.value.digitalEvidenceLink = data.digitalEvidenceLink as string
      loadedNorm.value.digitalEvidenceRelatedData =
        data.digitalEvidenceRelatedData as string
      loadedNorm.value.documentCategory = data.documentCategory as string
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
      loadedNorm.value.eli = data.eli as string
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
      loadedNorm.value.otherStatusNote = data.otherStatusNote as string
      loadedNorm.value.announcementDate = data.announcementDate as string
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

function formatDate(dateStrings: (string | undefined)[] | undefined): string {
  const dateString = Array.isArray(dateStrings) ? dateStrings[0] : dateStrings

  if (!dateString) {
    return ""
  }

  const date = new Date(dateString)
  const day = date.getDate().toString().padStart(2, "0")
  const month = (date.getMonth() + 1).toString().padStart(2, "0")
  const year = date.getFullYear().toString()
  return `${day}.${month}.${year}`
}

function citationDateSummarizer(data: Metadata): string {
  if (!data) return ""

  if (data.YEAR) {
    return data.YEAR.toString()
  }

  return formatDate(data.DATE)
}

function printAnnouncementSummary(data: Metadata): string {
  if (!data) return ""

  const announcementGazette = data.ANNOUNCEMENT_GAZETTE?.[0]
  const announcementYear = data.YEAR?.[0]
  const announcementNumber = data.NUMBER?.[0]
  const announcementPage = data.PAGE?.[0]
  const announcementAdditionalInfo = data.ADDITIONAL_INFO?.[0]
  const announcementExplanation = data.EXPLANATION?.[0]

  return `Papierverkündungsblatt | ${[
    announcementGazette,
    announcementYear,
    announcementNumber,
    announcementPage,
    announcementAdditionalInfo,
    announcementExplanation,
  ]
    .filter(Boolean)
    .join(", ")}`
}

function digitalAnnouncementSummary(data: Metadata): string {
  if (!data) return ""

  const announcementMedium = data.ANNOUNCEMENT_MEDIUM?.[0]
  const announcementDate = data.DATE?.[0]
  const announcementYear = data.YEAR?.[0]
  const announcementPage = data.PAGE?.[0]
  const announcementEdition = data.EDITION?.[0]
  const announcementAreaOfPub = data.AREA_OF_PUBLICATION?.[0]
  const announcementNumberOfPub =
    data.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0]
  const announcementAdditionalInfo = data.ADDITIONAL_INFO?.[0]
  const announcementExplanation = data.EXPLANATION?.[0]

  return `Elektronisches Verkündungsblatt | ${[
    announcementMedium,
    formatDate([announcementDate]),
    announcementEdition,
    announcementYear,
    announcementPage,
    announcementAreaOfPub,
    announcementNumberOfPub,
    announcementAdditionalInfo,
    announcementExplanation,
  ]
    .filter(Boolean)
    .join(", ")}`
}

function euAnnouncementSummary(data: Metadata): string {
  if (!data) return ""

  const euGazette = data.EU_GOVERNMENT_GAZETTE?.[0]
  const euYear = data.YEAR?.[0]
  const euSeries = data.SERIES?.[0]
  const euNumber = data.NUMBER?.[0]
  const euPage = data.PAGE?.[0]
  const euAdditionalInfo = data.ADDITIONAL_INFO?.[0]
  const euExplanation = data.EXPLANATION?.[0]

  return `Amtsblatt der EU | ${[
    euGazette,
    euYear,
    euSeries,
    euNumber,
    euPage,
    euAdditionalInfo,
    euExplanation,
  ]
    .filter(Boolean)
    .join(", ")}`
}

function otherOfficialReferenceSummary(data: Metadata): string {
  if (!data) return ""

  const otherOfficialReference = data.OTHER_OFFICIAL_REFERENCE?.[0] ?? ""

  return `Sonstige amtliche Fundstelle | ${otherOfficialReference}`
}

function officialReferenceSummarizer(data: MetadataSections): string {
  if (!data) return ""

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

const NORM_CATEGORY_TRANSLATIONS = {
  AMENDMENT_NORM: "Änderungsnorm",
  BASE_NORM: "Stammnorm",
  TRANSITIONAL_NORM: "Übergangsnorm",
}

function documentTypeSummarizer(data?: Metadata): VNode {
  const propertyNodes = []

  const typeName = data?.TYPE_NAME?.[0]
  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []
  const templateNames = data?.TEMPLATE_NAME ?? []

  propertyNodes.push(typeName)

  if (typeName && categories.length > 0) propertyNodes.push(h("div", "|"))

  categories.forEach((category) =>
    propertyNodes.push(
      h("div", { class: ["flex", "gap-4"] }, [
        h("img", { src: CheckMark, alt: "checkmark", width: "16" }),
        h("span", NORM_CATEGORY_TRANSLATIONS[category]),
      ])
    )
  )

  if ((typeName || categories.length > 0) && templateNames.length > 0)
    propertyNodes.push(h("div", "|"))

  templateNames.forEach((templateName) =>
    propertyNodes.push(
      h(
        "div",
        { class: ["bg-blue-500", "rounded-lg", "px-8", "py-4"] },
        templateName
      )
    )
  )

  return h(
    "div",
    { class: ["flex", "gap-8", "items-center", "flex-wrap"] },
    propertyNodes
  )
}

function divergentEntryIntoForceDefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const date = formatDate(data?.DATE)
  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []

  if (categories.length === 0 && date) {
    return h("div", {}, date)
  }

  const elements = []

  if (date) {
    elements.push(h("div", {}, date))
  }

  if (date && categories.length > 0) {
    elements.push(h("div", "|"))
  }

  categories.forEach((category) => {
    elements.push(
      h("div", { class: ["flex", "gap-8"] }, [
        h("img", {
          src: CheckMark,
          width: "16",
          alt: "Schwarzes Haken",
        }),
        h("span", {}, NORM_CATEGORY_TRANSLATIONS[category]),
      ])
    )
  })

  return h("div", { class: ["flex", "gap-8"] }, elements)
}

function getLabel(value: UndefinedDate): string {
  switch (value) {
    case UndefinedDate.UNDEFINED_UNKNOWN:
      return "unbestimmt (unbekannt)"
    case UndefinedDate.UNDEFINED_FUTURE:
      return "unbestimmt (zukünftig)"
    case UndefinedDate.UNDEFINED_NOT_PRESENT:
      return "nicht vorhanden"
    default:
      return ""
  }
}

function divergentEntryIntoForceUndefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const undefinedDate = data?.UNDEFINED_DATE?.[0]
  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []

  const elements = []

  if (categories.length === 0 && undefinedDate) {
    return h("div", {}, getLabel(undefinedDate))
  }

  if (undefinedDate) {
    elements.push(h("div", {}, getLabel(undefinedDate)))
  }

  if (undefinedDate && categories.length > 0) {
    elements.push(h("div", "|"))
  }

  categories.forEach((category) => {
    elements.push(
      h("div", { class: ["flex", "gap-8"] }, [
        h("img", {
          src: CheckMark,
          width: "16",
          alt: "Schwarzes Haken",
        }),
        h("span", {}, NORM_CATEGORY_TRANSLATIONS[category]),
      ])
    )
  })

  return h("div", { class: ["flex", "gap-8"] }, elements)
}

function DivergentEntryIntoForceSummarizer(
  data: MetadataSections
): VNode | string {
  if (!data) return ""

  if (data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED) {
    return divergentEntryIntoForceDefinedSummary(
      data.DIVERGENT_ENTRY_INTO_FORCE_DEFINED[0]
    )
  } else if (data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED) {
    return divergentEntryIntoForceUndefinedSummary(
      data.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED[0]
    )
  } else return ""
}

function DivergentExpirationSummarizer(data: MetadataSections): VNode | string {
  if (!data) return ""

  if (data.DIVERGENT_EXPIRATION_DEFINED) {
    return divergentEntryIntoForceDefinedSummary(
      data.DIVERGENT_EXPIRATION_DEFINED[0]
    )
  } else if (data.DIVERGENT_EXPIRATION_UNDEFINED) {
    return divergentEntryIntoForceUndefinedSummary(
      data.DIVERGENT_EXPIRATION_UNDEFINED[0]
    )
  } else return ""
}

function GeneralSummarizer(data: Metadata): string {
  if (!data) return ""

  const undefinedDate = data?.UNDEFINED_DATE?.[0]

  if (undefinedDate) {
    return getLabel(undefinedDate)
  } else {
    return formatDate(data.DATE)
  }
}

function participationSummarizer(data: Metadata) {
  if (!data) return ""

  const type = data.PARTICIPATION_TYPE?.[0]
  const institution = data.PARTICIPATION_INSTITUTION?.[0]

  return [type, institution]
    .filter((value) => value != "" && value != null)
    .join(" | ")
}

function subjectAreaSummarizer(data: Metadata) {
  if (!data) return ""

  const fna = data.SUBJECT_FNA?.[0]
  const previousFna = data.SUBJECT_PREVIOUS_FNA?.[0]
  const gesta = data.SUBJECT_GESTA?.[0]
  const bgb3 = data.SUBJECT_BGB_3?.[0]

  return [fna, previousFna, gesta, bgb3]
    .filter((value) => value != "" && value != null)
    .join(" | ")
}

const CitationDateSummary = withSummarizer(citationDateSummarizer)
const OfficialReferenceSummary = withSummarizer(officialReferenceSummarizer)
const NormProviderSummary = withSummarizer(normProviderSummarizer)
const DocumentTypeSummary = withSummarizer(documentTypeSummarizer)
const DivergentEntryIntoForceSummary = withSummarizer(
  DivergentEntryIntoForceSummarizer
)
const DivergentExpirationSummary = withSummarizer(DivergentExpirationSummarizer)
const GeneralSummary = withSummarizer(GeneralSummarizer)

const ParticipationSummary = withSummarizer(participationSummarizer)
const SubjectAreaSummary = withSummarizer(subjectAreaSummarizer)
</script>

<template>
  <div class="flex flex-col gap-8 max-w-screen-lg">
    <h1 class="h-[1px] overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>

    <SingleDataFieldSection
      id="officialLongTitle"
      v-model="flatMetadata.officialLongTitle"
      label="Amtliche Langüberschrift"
      :type="InputType.TEXTAREA"
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
      test-id="a11y-expandable-dataset"
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
      border-bottom
      :data-set="metadataSections.CITATION_DATE"
      :summary-component="CitationDateSummary"
      test-id="a11y-expandable-dataset"
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

    <fieldset class="mt-32">
      <legend id="otherFootnoteFields" class="heading-02-regular mb-[1rem]">
        Fußnoten
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
      border-bottom
      :data-set="metadataSections.AGE_INDICATION"
      test-id="a11y-expandable-dataset"
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
