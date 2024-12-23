<script setup lang="ts">
import { ref, toRaw, computed, watch } from "vue"
import { RouterLink } from "vue-router"
import SingleCategory from "@/components/category-import/SingleCategory.vue"
import IconBadge from "@/components/IconBadge.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { useValidationStore } from "@/composables/useValidationStore"
import DocumentUnit from "@/domain/documentUnit"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconSearch from "~icons/ic/baseline-search"

const props = defineProps<{
  documentNumber?: string
}>()
const store = useDocumentUnitStore()
const validationStore = useValidationStore<keyof typeof labels>()

const documentNumber = ref<string>(props.documentNumber ?? "")
const documentUnitToImport = ref<DocumentUnit | undefined>(undefined)
const errorMessage = ref<string | undefined>(undefined)
const statusBadge = computed(
  () => useStatusBadge(documentUnitToImport.value?.status).value,
)

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
      console.log("Importing active citations...")
      // Add specific logic for importing active citations
      break
    default:
      console.error("Unknown category")
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

function scrollToCategory(key: string) {
  const element = document.getElementById(key)
  if (element) {
    const headerOffset = 80
    const offsetPosition =
      element.getBoundingClientRect().top + window.scrollY - headerOffset
    window.scrollTo({ top: offsetPosition, behavior: "smooth" })
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
</script>

<template>
  <div data-testid="category-import">
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
      <!-- Todo: Wrap this docunit summary in a reusable component -->
      <span>
        <span>{{ documentUnitToImport.renderDecision }}</span>
        <IconBadge
          v-if="documentUnitToImport?.status"
          :background-color="statusBadge.backgroundColor"
          class="ml-4 inline-block"
          :color="statusBadge.color"
          :icon="toRaw(statusBadge.icon)"
          :label="statusBadge.label"
        />
        <span class="ds-label-01-reg ml-8 mr-8">|</span>
        <RouterLink
          class="nowrap ds-link-01-bold border-b-2 border-blue-800 leading-24 no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          tabindex="-1"
          target="_blank"
          :to="{
            name: 'caselaw-documentUnit-documentNumber-preview',
            params: { documentNumber: documentUnitToImport.documentNumber },
          }"
        >
          {{ documentUnitToImport.documentNumber }}
          <BaselineArrowOutward class="mb-4 inline w-24" />
        </RouterLink>
      </span>
      <div v-for="(value, key) in labels" :key="key">
        <SingleCategory
          :error-message="validationStore.getByField(key)"
          :handle-import="() => handleImport(key)"
          :has-content="
            !!(
              documentUnitToImport.contentRelatedIndexing[key] &&
              documentUnitToImport.contentRelatedIndexing[key].length > 0
            )
          "
          :label="value"
        />
      </div>
    </div>
  </div>
</template>
