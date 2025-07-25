<script setup lang="ts">
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { computed, onMounted, Ref, ref, watch } from "vue"
import { useRouter } from "vue-router"
import SingleCategory from "@/components/category-import/SingleCategory.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import ActiveCitation from "@/domain/activeCitation"
import { allLabels, contentRelatedIndexingLabels } from "@/domain/decision"
import { DocumentationUnit } from "@/domain/documentationUnit"
import NormReference from "@/domain/normReference"
import ParticipatingJudge from "@/domain/participatingJudge"
import Reference from "@/domain/reference"
import SingleNorm from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDecision } from "@/utils/typeGuards"
import IconSearch from "~icons/ic/baseline-search"

const props = defineProps<{
  documentNumber?: string
}>()
const store = useDocumentUnitStore()
const { documentUnit: targetDocumentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | null | undefined>
}
const validationStore = useValidationStore<keyof typeof allLabels>()

const documentNumber = ref<string>(props.documentNumber ?? "")
const sourceDocumentUnit = ref<DocumentationUnit | undefined>(undefined) as Ref<
  DocumentationUnit | null | undefined
>
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
  sourceDocumentUnit.value = undefined
  errorMessage.value = undefined

  if (!response.data) {
    errorMessage.value = "Keine Dokumentationseinheit gefunden."
    return
  }

  sourceDocumentUnit.value = response.data
}

const labels = computed(() => {
  return isDecision(targetDocumentUnit.value) &&
    isDecision(sourceDocumentUnit.value)
    ? allLabels
    : contentRelatedIndexingLabels
})

const hasContent = (key: keyof typeof allLabels): boolean => {
  if (isDecision(sourceDocumentUnit.value)) {
    if (key == "caselawReferences" || key == "literatureReferences") {
      const object = sourceDocumentUnit.value[key]
      return !!(Array.isArray(object) && object.length > 0)
    } else if (key in sourceDocumentUnit.value.shortTexts) {
      return !!sourceDocumentUnit.value.shortTexts[
        key as keyof typeof sourceDocumentUnit.value.shortTexts
      ]
    } else if (key in sourceDocumentUnit.value.longTexts) {
      const sourceLongText =
        sourceDocumentUnit.value.longTexts[
          key as keyof typeof sourceDocumentUnit.value.longTexts
        ]

      return (
        !!sourceLongText &&
        (!Array.isArray(sourceLongText) || sourceLongText.length > 0)
      )
    }
  }
  if (
    sourceDocumentUnit.value &&
    key in sourceDocumentUnit.value.contentRelatedIndexing
  ) {
    const object =
      sourceDocumentUnit.value.contentRelatedIndexing[
        key as keyof typeof sourceDocumentUnit.value.contentRelatedIndexing
      ]

    return !!(Array.isArray(object) && object.length > 0)
  }
  return false
}

const isImportable = (key: keyof typeof allLabels): boolean => {
  if (
    isDecision(targetDocumentUnit.value) &&
    isDecision(sourceDocumentUnit.value)
  )
    if (key in sourceDocumentUnit.value.shortTexts) {
      return !targetDocumentUnit.value!.shortTexts[
        key as keyof typeof sourceDocumentUnit.value.shortTexts
      ]
    } else if (key in sourceDocumentUnit.value.longTexts) {
      const targetLongText =
        targetDocumentUnit.value!.longTexts[
          key as keyof typeof sourceDocumentUnit.value.longTexts
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
const handleImport = async (key: keyof typeof allLabels) => {
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
    validationStore.add("Fehler beim Speichern der " + allLabels[key], key) // add an errormessage to the validationstore field with the key
  } else if (key == "caselawReferences" || key == "literatureReferences") {
    await router.push({
      name: "caselaw-documentUnit-documentNumber-references",
    })
  } else {
    scrollToCategory(key)
  }
}

function importReferences(key: "caselawReferences" | "literatureReferences") {
  const source = sourceDocumentUnit.value?.[key]
  if (source && isDecision(targetDocumentUnit.value)) {
    const targetReferences = targetDocumentUnit.value![key]

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
            !targetReferences.find((entry) =>
              isDuplicate(entry, reference, key),
            ),
        )
        .map(
          (reference) =>
            new Reference({ ...reference, id: crypto.randomUUID() }),
        )

      targetReferences.push(...uniqueImportableReferences)
    } else {
      targetDocumentUnit.value![key] = source.map(
        (reference) =>
          new Reference({
            ...reference,
            id: crypto.randomUUID(),
          }),
      )
    }
  }
}

function importKeywords() {
  const source = sourceDocumentUnit.value?.contentRelatedIndexing.keywords
  if (!source) return

  const targetKeywords =
    targetDocumentUnit.value!.contentRelatedIndexing.keywords

  if (targetKeywords) {
    const uniqueImportableKeywords = source.filter(
      (keyword) => !targetKeywords.includes(keyword),
    )
    targetKeywords.push(...uniqueImportableKeywords)
  } else {
    targetDocumentUnit.value!.contentRelatedIndexing.keywords = [...source]
  }
}

function importFieldsOfLaw() {
  const source = sourceDocumentUnit.value?.contentRelatedIndexing.fieldsOfLaw
  if (!source) return

  const targetFieldsOfLaw =
    targetDocumentUnit.value!.contentRelatedIndexing.fieldsOfLaw
  if (targetFieldsOfLaw) {
    const uniqueImportableFieldsOfLaw = source.filter(
      (fieldOfLaw) =>
        !targetFieldsOfLaw.find(
          (entry) => entry.identifier === fieldOfLaw.identifier,
        ),
    )
    targetFieldsOfLaw.push(...uniqueImportableFieldsOfLaw)
  } else {
    targetDocumentUnit.value!.contentRelatedIndexing.fieldsOfLaw = [...source]
  }
}

function importNorms() {
  const source = sourceDocumentUnit.value?.contentRelatedIndexing.norms
  if (!source) return

  const targetNorms = targetDocumentUnit.value!.contentRelatedIndexing.norms
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
    sourceDocumentUnit.value?.contentRelatedIndexing.activeCitations
  if (!source) return

  const targetActiveCitations =
    targetDocumentUnit.value!.contentRelatedIndexing.activeCitations ?? []

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

  targetDocumentUnit.value!.contentRelatedIndexing.activeCitations = [
    ...targetActiveCitations,
    ...uniqueImportableFieldsOfLaw,
  ] as ActiveCitation[]
}

function importParticipatingJudges() {
  if (
    isDecision(targetDocumentUnit.value) &&
    isDecision(sourceDocumentUnit.value)
  ) {
    const source = sourceDocumentUnit.value?.longTexts["participatingJudges"]
    if (!source) return
    source.forEach((judge) => (judge.id = undefined))

    targetDocumentUnit.value!.longTexts["participatingJudges"] = [
      ...source,
    ] as ParticipatingJudge[]
  }
}

function importShortTexts(key: string) {
  if (isDecision(targetDocumentUnit.value)) {
    const source =
      sourceDocumentUnit.value?.shortTexts[
        key as keyof typeof sourceDocumentUnit.value.shortTexts
      ]

    if (targetDocumentUnit.value)
      targetDocumentUnit.value.shortTexts[
        key as keyof typeof targetDocumentUnit.value.shortTexts
      ] = source
  }
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
  if (
    isDecision(targetDocumentUnit.value) &&
    isDecision(sourceDocumentUnit.value)
  ) {
    const source = sourceDocumentUnit.value?.longTexts[key]

    if (targetDocumentUnit.value) {
      targetDocumentUnit.value.longTexts[key] = source as string
    }
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
      v-if="sourceDocumentUnit"
      class="ris-label1-regular mt-24 flex flex-col gap-16 bg-blue-100 p-16"
    >
      <DecisionSummary
        :document-number="sourceDocumentUnit.documentNumber"
        :status="sourceDocumentUnit.status"
        :summary="sourceDocumentUnit.renderSummary"
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
