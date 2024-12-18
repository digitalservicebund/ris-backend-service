<script setup lang="ts">
import dayjs from "dayjs"
import { computed, Ref, ref } from "vue"
import { RouterLink } from "vue-router"
import ImportSingleCategory from "@/components/category-import/ImportSingleCategory.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import IconBadge from "@/components/IconBadge.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import LinkElement from "@/components/LinkElement.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconSearch from "~icons/ic/baseline-search"

const documentNumber = ref("")
const documentUnitToImport = ref<DocumentUnit | undefined>(undefined)
const searchErrorMessage = ref<string | undefined>(undefined)

const store = useDocumentUnitStore()
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
    searchErrorMessage.value = undefined
  } else {
    documentUnitToImport.value = undefined
    searchErrorMessage.value = "Keine Dokumentationseinheit gefunden."
  }
}

const existingKeywords = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.keywords ?? [],
  set: (newValues: string[]) => {
    store.documentUnit!.contentRelatedIndexing.keywords = newValues
  },
})

const existingFieldsOfLaw = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.fieldsOfLaw ?? [],
  set: (newValues: FieldOfLaw[]) => {
    store.documentUnit!.contentRelatedIndexing.fieldsOfLaw = newValues
  },
})

const importableKeywords = computed(
  () => documentUnitToImport?.value?.contentRelatedIndexing?.keywords ?? [],
)
const importableFieldsOfLaw = computed(
  () => documentUnitToImport?.value?.contentRelatedIndexing?.fieldsOfLaw ?? [],
)
const canImportKeywords = computed(() => importableKeywords.value.length > 0)
const canImportFieldsOfLaw = computed(
  () => importableFieldsOfLaw.value.length > 0,
)
const copySuccessKeywords = ref(false)
const errorMessageKeywords = ref<string | undefined>(undefined)
const copySuccessFieldsOfLaw = ref(false)
const errorMessageFieldsOfLaw = ref<string | undefined>(undefined)

async function importKeywords() {
  hideErrors()
  if (!canImportKeywords.value) return
  const uniqueImportableKeywords = importableKeywords.value.filter(
    (keyword) => !existingKeywords.value.includes(keyword),
  )
  existingKeywords.value.push(...uniqueImportableKeywords)

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    searchErrorMessage.value = "Fehler beim Speichern der Schlagwörter"
  } else {
    displaySuccess(copySuccessKeywords)
    scrollToCategory(DocumentUnitCategoriesEnum.KEYWORDS)
  }
}

async function importFieldsOfLaw() {
  hideErrors()
  if (!canImportFieldsOfLaw.value) return
  const uniqueImportableFieldsOfLaw = importableFieldsOfLaw.value.filter(
    (fieldOfLaw) =>
      !existingFieldsOfLaw.value.find(
        (entry) => entry.identifier === fieldOfLaw.identifier,
      ),
  )
  existingFieldsOfLaw.value.push(...uniqueImportableFieldsOfLaw)

  const updateResponse = await store.updateDocumentUnit()
  if (updateResponse.error) {
    searchErrorMessage.value = "Fehler beim Speichern der Sachgebiete"
  } else {
    displaySuccess(copySuccessFieldsOfLaw)
    scrollToCategory(DocumentUnitCategoriesEnum.FIELDS_OF_LAW)
  }
}

function displaySuccess(ref: Ref) {
  ref.value = true
  setTimeout(() => {
    ref.value = false
  }, 7000)
}

function scrollToCategory(category: DocumentUnitCategoriesEnum) {
  const element = document.getElementById(category)
  const headerOffset = 80
  const elementPosition = element ? element.getBoundingClientRect().top : 0
  const offsetPosition = elementPosition + window.scrollY - headerOffset
  window.scrollTo({
    top: offsetPosition,
    behavior: "smooth",
  })
}

function hideErrors() {
  errorMessageKeywords.value = undefined
  errorMessageFieldsOfLaw.value = undefined
}
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
        ></TextInput>
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

    <span v-if="searchErrorMessage" class="ds-label-02-reg text-red-800">{{
      searchErrorMessage
    }}</span>

    <div
      v-if="documentUnitToImport"
      class="ds-label-02-reg mt-24 flex flex-col gap-16 bg-blue-100 p-16"
    >
      <RouterLink
        v-if="documentUnitToImport.documentNumber"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: { documentNumber: documentUnitToImport.documentNumber },
        }"
      >
        <LinkElement :title="documentUnitToImport.documentNumber" />
      </RouterLink>

      <IconBadge v-bind="useStatusBadge(documentUnitToImport.status).value" />

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900"> Gericht</span>
        {{ documentUnitToImport.coreData?.court?.label }}
      </div>

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900"> Aktenzeichen</span>
        {{ documentUnitToImport.coreData?.fileNumbers?.join(", ") }}
      </div>

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900">Entscheidungsdatum</span>
        {{
          dayjs(documentUnitToImport.coreData?.decisionDate).format(
            "DD.MM.YYYY",
          )
        }}
      </div>

      <ImportSingleCategory
        :error-message="errorMessageKeywords"
        :import-success="copySuccessKeywords"
        :importable="canImportKeywords"
        label="Schlagwörter"
        @import="importKeywords"
      />

      <ImportSingleCategory
        :error-message="errorMessageFieldsOfLaw"
        :import-success="copySuccessFieldsOfLaw"
        :importable="canImportFieldsOfLaw"
        label="Sachgebiete"
        @import="importFieldsOfLaw"
      />
    </div>
  </div>
</template>
