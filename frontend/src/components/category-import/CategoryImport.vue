<script setup lang="ts">
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { onMounted, Ref, ref, watch } from "vue"
import { useRouter } from "vue-router"
import SingleCategory from "@/components/category-import/SingleCategory.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import ActiveCitation from "@/domain/activeCitation"
import { Decision } from "@/domain/decision"
import { longTextLabels, shortTextLabels } from "@/domain/documentUnit"
import NormReference from "@/domain/normReference"
import ParticipatingJudge from "@/domain/participatingJudge"
import Reference from "@/domain/reference"
import SingleNorm from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDocumentUnit } from "@/utils/typeGuards"
import IconSearch from "~icons/ic/baseline-search"

const props = defineProps<{
  documentNumber?: string
}>()
const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const validationStore = useValidationStore<keyof typeof labels>()

const documentNumber = ref<string>(props.documentNumber ?? "")
const documentUnitToImport = ref<Decision | undefined>(undefined)
const errorMessage = ref<string | undefined>(undefined)
const router = useRouter()

/**
 * Loads the document unit to import category data from.
 * Displays an error message, if no document unit could be found with the given document number.
 */
async function searchForDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    documentNumber.value,
  )
  if (isDocumentUnit(response.data)) {
    documentUnitToImport.value = response.data
    errorMessage.value = undefined
  } else {
    documentUnitToImport.value = undefined
    errorMessage.value = "Keine Dokumentationseinheit gefunden."
  }
}

const labels = {
  caselawReferences: "Rechtsprechungsfundstellen",
  literatureReferences: "Literaturfundstellen",
  keywords: "Schlagwörter",
  fieldsOfLaw: "Sachgebiete",
  norms: "Normen",
  activeCitations: "Aktivzitierung",
  ...shortTextLabels,
  ...longTextLabels,
}

const hasContent = (key: keyof typeof labels): boolean => {
  if (documentUnitToImport.value)
    if (key == "caselawReferences" || key == "literatureReferences") {
      const object = documentUnitToImport.value[key]
      return !!(Array.isArray(object) && object.length > 0)
    } else if (key in documentUnitToImport.value.contentRelatedIndexing) {
      const object =
        documentUnitToImport.value.contentRelatedIndexing[
          key as keyof typeof documentUnitToImport.value.contentRelatedIndexing
        ]

      return !!(Array.isArray(object) && object.length > 0)
    } else if (key in documentUnitToImport.value.shortTexts) {
      return !!documentUnitToImport.value.shortTexts[
        key as keyof typeof documentUnitToImport.value.shortTexts
      ]
    } else if (key in documentUnitToImport.value.longTexts) {
      const sourceLongText =
        documentUnitToImport.value.longTexts[
          key as keyof typeof documentUnitToImport.value.longTexts
        ]

      return (
        !!sourceLongText &&
        (!Array.isArray(sourceLongText) || sourceLongText.length > 0)
      )
    }
  return false
}

const isImportable = (key: keyof typeof labels): boolean => {
  if (documentUnitToImport.value)
    if (key in documentUnitToImport.value.shortTexts) {
      return !documentUnit.value!.shortTexts[
        key as keyof typeof documentUnitToImport.value.shortTexts
      ]
    } else if (key in documentUnitToImport.value.longTexts) {
      const targetLongText =
        documentUnit.value!.longTexts[
          key as keyof typeof documentUnitToImport.value.longTexts
        ]

      const isEmptyText =
        typeof targetLongText === "string" && targetLongText.trim().length > 0
      const isEmptyArray =
        Array.isArray(targetLongText) && targetLongText.length > 0
      return !isEmptyText && !isEmptyArray
    }
  return true
}

// Handle import logic
const handleImport = async (key: keyof typeof labels) => {
  validationStore.reset()

  switch (key) {
    case "caselawReferences":
    case "literatureReferences":
      importReferences(key)
      break
    case "keywords":
      importKeywords()
      break
    case "fieldsOfLaw":
      importFieldsOfLaw()
      break
    case "norms":
      importNorms()
      break
    case "activeCitations":
      importActiveCitations()
      break
    case "headline":
    case "guidingPrinciple":
    case "headnote":
    case "otherHeadnote":
      importShortTexts(key)
      break
    case "tenor":
    case "reasons":
    case "caseFacts":
    case "decisionReasons":
    case "dissentingOpinion":
    case "otherLongText":
    case "outline":
      importLongTexts(key)
      break
    case "participatingJudges":
      importParticipatingJudges()
      break
  }

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    validationStore.add("Fehler beim Speichern der " + labels[key], key) // add an errormessage to the validationstore field with the key
  } else if (key == "caselawReferences" || key == "literatureReferences") {
    await router.push({
      name: "caselaw-documentUnit-documentNumber-references",
    })
  } else {
    scrollToCategory(key)
  }
}

function importReferences(key: "caselawReferences" | "literatureReferences") {
  const source = documentUnitToImport.value?.[key]
  if (!source) return

  const targetReferences = documentUnit.value![key]

  if (targetReferences) {
    const isDuplicate = (
      entry: Reference,
      reference: Reference,
      key: string,
    ) => {
      const sameLegalPeriodical =
        entry.legalPeriodical?.uuid === reference.legalPeriodical?.uuid ||
        entry.legalPeriodicalRawValue === reference.legalPeriodicalRawValue

      const sameCitation = entry.citation === reference.citation

      const sameReferenceSupplement =
        key !== "caselawReferences" ||
        entry.referenceSupplement === reference.referenceSupplement

      const sameAuthor =
        key !== "literatureReferences" || entry.author === reference.author

      const sameDocumentType =
        key !== "literatureReferences" ||
        entry.documentType?.uuid === reference.documentType?.uuid

      return (
        sameLegalPeriodical &&
        sameCitation &&
        sameReferenceSupplement &&
        sameAuthor &&
        sameDocumentType
      )
    }
    const uniqueImportableReferences = source
      .filter(
        (reference) =>
          !targetReferences.find((entry) => isDuplicate(entry, reference, key)),
      )
      .map(
        (reference) => new Reference({ ...reference, id: crypto.randomUUID() }),
      )

    targetReferences.push(...uniqueImportableReferences)
  } else {
    documentUnit.value![key] = source.map(
      (reference) =>
        new Reference({
          ...reference,
          id: crypto.randomUUID(),
        }),
    )
  }
}

function importKeywords() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.keywords
  if (!source) return

  const targetKeywords = documentUnit.value!.contentRelatedIndexing.keywords

  if (targetKeywords) {
    const uniqueImportableKeywords = source.filter(
      (keyword) => !targetKeywords.includes(keyword),
    )
    targetKeywords.push(...uniqueImportableKeywords)
  } else {
    documentUnit.value!.contentRelatedIndexing.keywords = [...source]
  }
}

function importFieldsOfLaw() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.fieldsOfLaw
  if (!source) return

  const targetFieldsOfLaw =
    documentUnit.value!.contentRelatedIndexing.fieldsOfLaw
  if (targetFieldsOfLaw) {
    const uniqueImportableFieldsOfLaw = source.filter(
      (fieldOfLaw) =>
        !targetFieldsOfLaw.find(
          (entry) => entry.identifier === fieldOfLaw.identifier,
        ),
    )
    targetFieldsOfLaw.push(...uniqueImportableFieldsOfLaw)
  } else {
    documentUnit.value!.contentRelatedIndexing.fieldsOfLaw = [...source]
  }
}

function importNorms() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.norms
  if (!source) return

  const targetNorms = documentUnit.value!.contentRelatedIndexing.norms
  if (targetNorms) {
    source.forEach((importableNorm) => {
      // first check for abbreviation, then for raw value
      const existingWithAbbreviation = targetNorms.find((existing) =>
        importableNorm.normAbbreviation?.abbreviation != null
          ? existing.normAbbreviation?.abbreviation ===
            importableNorm.normAbbreviation?.abbreviation
          : existing.normAbbreviationRawValue ===
            importableNorm.normAbbreviationRawValue,
      )
      if (existingWithAbbreviation) {
        //import single norms into existing norm reference
        const singleNorms = existingWithAbbreviation?.singleNorms
        if (singleNorms && importableNorm.singleNorms) {
          importableNorm.singleNorms
            .filter(
              (importableSingleNorm) =>
                !singleNorms.some(
                  (singleNorm) =>
                    singleNorm.singleNorm === importableSingleNorm.singleNorm,
                ),
            )
            .map((importableSingleNorm) => {
              singleNorms.push(
                new SingleNorm({
                  ...(importableSingleNorm as SingleNorm),
                  id: undefined,
                }),
              )
            })
        }
      } else {
        // import entire norm reference
        importableNorm.singleNorms?.forEach(
          (singleNorm) => (singleNorm.id = undefined),
        )
        targetNorms.push(
          new NormReference({ ...(importableNorm as NormReference) }),
        )
      }
    })
  }
}

function importActiveCitations() {
  const source =
    documentUnitToImport.value?.contentRelatedIndexing.activeCitations
  if (!source) return

  const targetActiveCitations =
    documentUnit.value!.contentRelatedIndexing.activeCitations ?? []

  const uniqueImportableFieldsOfLaw = source
    .filter(
      (activeCitation) =>
        !targetActiveCitations.find(
          (entry) =>
            entry.documentNumber === activeCitation.documentNumber &&
            entry.citationType?.uuid === activeCitation.citationType?.uuid,
        ),
    )
    .map((activeCitation) => ({
      ...activeCitation,
      uuid: crypto.randomUUID(),
      newEntry: true,
    }))

  documentUnit.value!.contentRelatedIndexing.activeCitations = [
    ...targetActiveCitations,
    ...uniqueImportableFieldsOfLaw,
  ] as ActiveCitation[]
}

function importParticipatingJudges() {
  const source = documentUnitToImport.value?.longTexts["participatingJudges"]
  if (!source) return

  source.forEach((judge) => (judge.id = undefined))

  documentUnit.value!.longTexts["participatingJudges"] = [
    ...source,
  ] as ParticipatingJudge[]
}

function importShortTexts(key: string) {
  const source =
    documentUnitToImport.value?.shortTexts[
      key as keyof typeof documentUnitToImport.value.shortTexts
    ]

  if (documentUnit.value)
    documentUnit.value.shortTexts[
      key as keyof typeof documentUnit.value.shortTexts
    ] = source
}

// By narrowing the type of key to exclude "participatingJudges", TypeScript no longer considers the possibility of assigning a non-string value to documentUnit.value.longTexts[key].
type StringKeys =
  | "tenor"
  | "reasons"
  | "caseFacts"
  | "decisionReasons"
  | "dissentingOpinion"
  | "otherLongText"
  | "outline"

function importLongTexts(key: StringKeys) {
  const source = documentUnitToImport.value?.longTexts[key]

  if (documentUnit.value) {
    documentUnit.value.longTexts[key] = source as string
  }
}

function scrollToCategory(key: string) {
  const element = document.getElementById(key)
  if (element) {
    const headerOffset = 80
    const offsetPosition =
      element.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top: offsetPosition, behavior: "smooth" })
  }
}

function adjustContainerHeight() {
  const container = document.querySelector(
    ".scrollable-container",
  ) as HTMLElement
  if (container) {
    const topOffset = container.getBoundingClientRect().top // Distance from the top of the viewport
    container.style.height = `calc(100vh - ${topOffset}px)` // Dynamically set height
  }
}

watch(
  () => props.documentNumber,
  async () => {
    documentNumber.value = props.documentNumber ?? ""
    if (props.documentNumber) {
      await searchForDocumentUnit()
    }
  },
  { immediate: true },
)

onMounted(() => {
  adjustContainerHeight()
})
</script>

<template>
  <div
    class="scrollable-container relative overflow-auto"
    data-testid="category-import"
  >
    <span class="ris-label1-bold">Rubriken importieren</span>
    <div class="mt-16 flex flex-row items-end gap-8">
      <InputField
        id="categoryImporterDocumentNumber"
        v-slot="slotProps"
        label="Dokumentnummer"
      >
        <InputText
          id="categoryImporterDocumentNumber"
          v-model="documentNumber"
          aria-label="Dokumentnummer Eingabefeld"
          fluid
          :ivalid="slotProps.hasError"
          size="small"
          @enter-released="searchForDocumentUnit"
        />
      </InputField>

      <Button
        aria-label="Dokumentationseinheit laden"
        :disabled="documentNumber?.length != 13"
        @click="searchForDocumentUnit"
        ><template #icon> <IconSearch /> </template
      ></Button>
    </div>

    <span v-if="errorMessage" class="ris-label2-regular text-red-800">{{
      errorMessage
    }}</span>

    <div
      v-if="documentUnitToImport"
      class="ris-label1-regular mt-24 flex flex-col gap-16 bg-blue-100 p-16"
    >
      <DecisionSummary
        :document-number="documentUnitToImport.documentNumber"
        :status="documentUnitToImport.status"
        :summary="documentUnitToImport.renderSummary"
      />
      <div v-for="(value, key) in labels" :key="key">
        <SingleCategory
          :error-message="validationStore.getByField(key)"
          :handle-import="() => handleImport(key)"
          :has-content="hasContent(key)"
          :importable="isImportable(key)"
          :label="value"
        />
      </div>
    </div>
  </div>
</template>
