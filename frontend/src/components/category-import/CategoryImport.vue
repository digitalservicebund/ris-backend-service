<script setup lang="ts">
import { ref, watch, onMounted } from "vue"
import SingleCategory from "@/components/category-import/SingleCategory.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import DocumentUnit from "@/domain/documentUnit"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconSearch from "~icons/ic/baseline-search"

const props = defineProps<{
  documentNumber?: string
}>()
const store = useDocumentUnitStore()
const validationStore = useValidationStore<keyof typeof labels>()

const documentNumber = ref<string>(props.documentNumber ?? "")
const documentUnitToImport = ref<DocumentUnit | undefined>(undefined)
const errorMessage = ref<string | undefined>(undefined)

/**
 * Loads the document unit to import category data from.
 * Displays an error message, if no document unit could be found with the given document number.
 */
async function searchForDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    documentNumber.value,
  )
  if (response.data) {
    documentUnitToImport.value = response.data
    errorMessage.value = undefined
  } else {
    documentUnitToImport.value = undefined
    errorMessage.value = "Keine Dokumentationseinheit gefunden."
  }
}

const labels = {
  keywords: "Schlagwörter",
  fieldsOfLaw: "Sachgebiete",
  norms: "Normen",
  activeCitations: "Aktivzitierung",
  headline: "Titelzeile",
  guidingPrinciple: "Leitsatz",
  headnote: "Orientierungssatz",
  otherHeadnote: "Sonstiger Orientierungssatz",
  tenor: "Tenor",
  reasons: "Gründe",
  caseFacts: "Tatbestand",
  decisionReasons: "Entscheidungsgründe",
  otherLongText: "Sonstiger Langtext",
  dissentingOpinion: "Abweichende Meinung",
  participatingJudges: "Mitwirkende Richter",
  outline: "Gliederung",
}

const hasContent = (key: keyof typeof labels): boolean => {
  if (documentUnitToImport.value)
    if (key in documentUnitToImport.value.contentRelatedIndexing) {
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
      const longText =
        documentUnitToImport.value.longTexts[
          key as keyof typeof documentUnitToImport.value.longTexts
        ]

      if (typeof longText === "string" && longText.trim().length > 0) {
        return true
      }
      if (Array.isArray(longText) && longText.length > 0) {
        return true
      }
    }
  return false
}

const isImportable = (key: keyof typeof labels): boolean => {
  if (documentUnitToImport.value)
    if (key in documentUnitToImport.value.shortTexts) {
      return !store.documentUnit!.shortTexts[
        key as keyof typeof documentUnitToImport.value.shortTexts
      ]
    } else if (key in documentUnitToImport.value.longTexts) {
      const longText =
        store.documentUnit!.longTexts[
          key as keyof typeof documentUnitToImport.value.longTexts
        ]

      if (typeof longText === "string" && longText.trim().length > 0) {
        return false
      }
      if (Array.isArray(longText) && longText.length > 0) {
        return false
      }
    }
  return true
}

// Handle import logic
const handleImport = async (key: keyof typeof labels) => {
  validationStore.reset()

  switch (key) {
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
  } else {
    scrollToCategory(key)
  }
}

function importKeywords() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.keywords
  if (!source) return

  const targetKeywords = store.documentUnit!.contentRelatedIndexing.keywords

  if (targetKeywords) {
    const uniqueImportableKeywords = source.filter(
      (keyword) => !targetKeywords.includes(keyword),
    )
    targetKeywords.push(...uniqueImportableKeywords)
  } else {
    store.documentUnit!.contentRelatedIndexing.keywords = [...source]
  }
}

function importFieldsOfLaw() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.fieldsOfLaw
  if (!source) return

  const targetFieldsOfLaw =
    store.documentUnit!.contentRelatedIndexing.fieldsOfLaw
  if (targetFieldsOfLaw) {
    const uniqueImportableFieldsOfLaw = source.filter(
      (fieldOfLaw) =>
        !targetFieldsOfLaw.find(
          (entry) => entry.identifier === fieldOfLaw.identifier,
        ),
    )
    targetFieldsOfLaw.push(...uniqueImportableFieldsOfLaw)
  } else {
    store.documentUnit!.contentRelatedIndexing.fieldsOfLaw = [...source]
  }
}

function importNorms() {
  const source = documentUnitToImport.value?.contentRelatedIndexing.norms
  if (!source) return

  const targetNorms = store.documentUnit!.contentRelatedIndexing.norms
  if (targetNorms) {
    source.forEach((importableNorm) => {
      const existingWithAbbreviation = targetNorms.find(
        (existing) =>
          existing.normAbbreviation?.abbreviation ===
          importableNorm.normAbbreviation?.abbreviation,
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
    store.documentUnit!.contentRelatedIndexing.activeCitations
  if (targetActiveCitations) {
    // consider as duplicate, if real reference found with same docnumber and citation
    const uniqueImportableFieldsOfLaw = source.filter(
      (activeCitation) =>
        !targetActiveCitations.find(
          (entry) =>
            entry.documentNumber === activeCitation.documentNumber &&
            entry.citationType?.uuid === activeCitation.citationType?.uuid,
        ),
    )
    targetActiveCitations.push(...uniqueImportableFieldsOfLaw)
  } else {
    store.documentUnit!.contentRelatedIndexing.activeCitations = [...source]
  }
}

function importParticipatingJudges() {
  const source = documentUnitToImport.value?.longTexts["participatingJudges"]
  if (!source) return

  source.forEach((judge) => (judge.id = undefined))

  store.documentUnit!.longTexts["participatingJudges"] = [...source]
}

function importShortTexts(key: string) {
  const source =
    documentUnitToImport.value?.shortTexts[
      key as keyof typeof documentUnitToImport.value.shortTexts
    ]

  if (store.documentUnit)
    store.documentUnit.shortTexts[
      key as keyof typeof store.documentUnit.shortTexts
    ] = source
}

// By narrowing the type of key to exclude "participatingJudges", TypeScript no longer considers the possibility of assigning a non-string value to store.documentUnit.longTexts[key].
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

  if (store.documentUnit) {
    store.documentUnit.longTexts[key] = source as string
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
    <span class="ds-label-01-bold">Rubriken importieren</span>
    <div class="mt-16 flex flex-row items-end gap-8">
      <InputField
        id="categoryImporterDocumentNumber"
        v-slot="slotProps"
        label="Dokumentnummer"
      >
        <TextInput
          id="categoryImporterDocumentNumber"
          v-model="documentNumber"
          aria-label="Dokumentnummer Eingabefeld"
          :has-error="slotProps.hasError"
          size="medium"
          @enter-released="searchForDocumentUnit"
        />
      </InputField>

      <TextButton
        aria-label="Dokumentationseinheit laden"
        button-type="primary"
        :disabled="documentNumber?.length != 13"
        :icon="IconSearch"
        size="medium"
        @click="searchForDocumentUnit"
      />
    </div>

    <span v-if="errorMessage" class="ds-label-02-reg text-red-800">{{
      errorMessage
    }}</span>

    <div
      v-if="documentUnitToImport"
      class="ds-label-01-reg mt-24 flex flex-col gap-16 bg-blue-100 p-16"
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
