<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs, ref, watch, h, VNode, createTextVNode } from "vue"
import { useRoute } from "vue-router"
import CheckMark from "@/assets/icons/ckeckbox_regular.svg"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import AnnouncementDateInputGroup from "@/components/announcementDate/AnnouncementDateInputGroup.vue"
import { summarizeAnnouncementDate } from "@/components/announcementDate/summarizer"
import AnnouncementGroup from "@/components/AnnouncementGroup.vue"
import CategorizedReferenceInputGroup from "@/components/CategorizedReferenceInputGroup.vue"
import CitationDateInputGroup from "@/components/CitationDateInputGroup.vue"
import DigitalEvidenceInputGroup from "@/components/DigitalEvidenceInputGroup.vue"
import DivergentEntryIntoForceGroup from "@/components/DivergentEntryIntoForceGroup.vue"
import DivergentExpirationGroup from "@/components/DivergentExpirationGroup.vue"
import DocumentStatusGroup from "@/components/DocumentStatusGroup.vue"
import DocumentTypeInputGroup from "@/components/DocumentTypeInputGroup.vue"
import EntryIntoForceInputGroup from "@/components/EntryIntoForceInputGroup.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import ExpirationInputGroup from "@/components/ExpirationInputGroup.vue"
import FootnoteInput from "@/components/footnotes/FootnoteInput.vue"
import { summarizeFootnotePerLine } from "@/components/footnotes/summarizer"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import NormProviderInputGroup from "@/components/NormProviderInputGroup.vue"
import ParticipatingInstitutionInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import PrincipleEntryIntoForceInputGroup from "@/components/PrincipleEntryIntoForceInputGroup.vue"
import PrincipleExpirationInputGroup from "@/components/PrincipleExpirationInputGroup.vue"
import PublicationDateInputGroup from "@/components/PublicationDateInputGroup.vue"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import StatusIndicationInputGroup from "@/components/statusIndication/StatusIndicationInputGroup.vue"
import { summarizeStatusIndication } from "@/components/statusIndication/summarizer"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import {
  FlatMetadata,
  Metadata,
  MetadataSections,
  UndefinedDate,
} from "@/domain/Norm"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"
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
      loadedNorm.value.eli = data.eli as string
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
  if (data) {
    const midSection = [
      data.ANNOUNCEMENT_GAZETTE?.[0],
      data.YEAR?.[0],
      data.NUMBER?.[0],
      data.PAGE?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Papierverkündungsblatt",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
}

function digitalAnnouncementSummary(data: Metadata): string {
  if (data) {
    const midSection = [
      data.ANNOUNCEMENT_MEDIUM?.[0],
      formatDate([data.DATE?.[0]]),
      data.YEAR?.[0],
      data.PAGE?.[0],
      data.EDITION?.[0],
      data.AREA_OF_PUBLICATION?.[0],
      data.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Elektronisches Verkündungsblatt",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
}

function euAnnouncementSummary(data: Metadata): string {
  if (data) {
    const midSection = [
      data.EU_GOVERNMENT_GAZETTE?.[0],
      data.YEAR?.[0],
      data.SERIES?.[0],
      data.NUMBER?.[0],
      data.PAGE?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Amtsblatt der EU",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
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

function documentStatusSummary(data: Metadata): string {
  const PROOF_INDICATION_TRANSLATIONS = {
    NOT_YET_CONSIDERED: "noch nicht berücksichtigt",
    CONSIDERED: "ist berücksichtigt",
  }

  if (!data) return ""

  const workNote = data?.WORK_NOTE ?? []
  const description = data?.DESCRIPTION?.[0]
  const date = formatDate([data?.DATE?.[0]])
  const year = data?.YEAR?.[0]
  const reference = data?.REFERENCE?.[0]
  const entryIntoForceDateState = data?.ENTRY_INTO_FORCE_DATE_NOTE ?? []
  const proofIndication =
    data?.PROOF_INDICATION?.filter((category) => category != null) ?? []

  const translatedProofIndication = proofIndication.map(
    (indication) => PROOF_INDICATION_TRANSLATIONS[indication] || indication
  )
  const resultArray = []

  if (workNote) resultArray.push(...workNote)
  if (description) resultArray.push(description)
  if (date) resultArray.push(date)
  if (year) resultArray.push(year)
  if (reference) resultArray.push(reference)
  if (entryIntoForceDateState) resultArray.push(...entryIntoForceDateState)

  resultArray.push(...translatedProofIndication)

  return resultArray.join(" ")
}

function documentTextProofSummary(data: Metadata): string {
  const PROOF_TYPE_TRANSLATIONS = {
    TEXT_PROOF_FROM: "Textnachweis ab",
    TEXT_PROOF_VALIDITY_FROM: "Textnachweis Geltung ab",
  }

  if (!data) return ""

  const proofType =
    data?.PROOF_TYPE?.filter((category) => category != null) ?? []
  const text = data?.TEXT?.[0]

  const translatedProofType = proofType.map(
    (type) => PROOF_TYPE_TRANSLATIONS[type] || type
  )
  const resultArray = [...translatedProofType]

  if (text) {
    resultArray.push(text)
  }

  return resultArray.join(" ")
}

function documentOtherSummary(data: Metadata): string {
  const OTHER_TYPE_TRANSLATIONS = {
    TEXT_IN_PROGRESS: "Text in Bearbeitung",
    TEXT_PROOFED_BUT_NOT_DONE:
      "Nachgewiesener Text dokumentarisch noch nicht abschließend bearbeitet",
  }

  if (!data) return ""

  const otherType =
    data?.OTHER_TYPE?.filter((category) => category != null) ?? []

  const translatedOtherType = otherType.map(
    (type) => OTHER_TYPE_TRANSLATIONS[type] || type
  )

  return translatedOtherType.join(" ")
}

function documentStatusSectionSummarizer(data: MetadataSections): string {
  if (!data) return ""

  if (data.DOCUMENT_STATUS) {
    return documentStatusSummary(data.DOCUMENT_STATUS[0])
  } else if (data.DOCUMENT_TEXT_PROOF) {
    return documentTextProofSummary(data.DOCUMENT_TEXT_PROOF[0])
  } else if (data.DOCUMENT_OTHER) {
    return documentOtherSummary(data.DOCUMENT_OTHER[0])
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

  return [
    fna && `FNA-Nummer ${fna}`,
    previousFna && `Frühere FNA-Nummer ${previousFna}`,
    gesta && `GESTA-Nummer ${gesta}`,
    bgb3 && `Bundesgesetzblatt Teil III ${bgb3}`,
  ]
    .filter(Boolean)
    .join(" | ")
}

const CitationDateSummary = withSummarizer(citationDateSummarizer)
const OfficialReferenceSummary = withSummarizer(officialReferenceSummarizer)
const DocumentStatusSectionSummary = withSummarizer(
  documentStatusSectionSummarizer
)
const NormProviderSummary = withSummarizer(normProviderSummarizer)
const DocumentTypeSummary = withSummarizer(documentTypeSummarizer)
const DivergentEntryIntoForceSummary = withSummarizer(
  DivergentEntryIntoForceSummarizer
)
const DivergentExpirationSummary = withSummarizer(DivergentExpirationSummarizer)
const GeneralSummary = withSummarizer(GeneralSummarizer)

const ParticipationSummary = withSummarizer(participationSummarizer)
const SubjectAreaSummary = withSummarizer(subjectAreaSummarizer)
const footnoteLineSummary = withSummarizer(summarizeFootnotePerLine)
const StatusIndicationSummary = withSummarizer(summarizeStatusIndication)
const AnnouncementDateSummary = withSummarizer(summarizeAnnouncementDate)
</script>

<template>
  <div class="flex flex-col gap-8 max-w-screen-lg">
    <h1 class="h-[1px] overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>

    <SingleDataFieldSection
      id="officialLongTitle"
      v-model="officialLongTitle"
      label="Amtliche Langüberschrift"
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
      :summary-component="CitationDateSummary"
      test-id="a11y-expandable-dataset"
      title="Veröffentlichungsdatum"
    >
      <EditableList
        v-model="metadataSections.PUBLICATION_DATE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="PublicationDateInputGroup"
        :summary-component="CitationDateSummary"
      />
    </ExpandableDataSet>

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
      test-id="a11y-expandable-dataset"
      title="Elektronischer Nachweis"
    >
      <EditableList
        v-model="metadataSections.DIGITAL_EVIDENCE"
        :default-value="{}"
        disable-multi-entry
        :edit-component="DigitalEvidenceInputGroup"
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

    <SingleDataFieldSection id="text" v-model="text" label="Text" />

    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
