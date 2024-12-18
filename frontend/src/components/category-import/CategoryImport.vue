<script setup lang="ts">
import dayjs from "dayjs"
import { ref } from "vue"
import { RouterLink } from "vue-router"
import ImportFieldsOfLaw from "@/components/category-import/ImportFieldsOfLaw.vue"
import ImportKeywords from "@/components/category-import/ImportKeywords.vue"
import IconBadge from "@/components/IconBadge.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import LinkElement from "@/components/LinkElement.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import IconSearch from "~icons/ic/baseline-search"

const documentNumber = ref("")
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

    <span v-if="errorMessage" class="ds-label-02-reg text-red-800">{{
      errorMessage
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

      <ImportKeywords
        :importable-keywords="
          documentUnitToImport.contentRelatedIndexing.keywords ?? []
        "
      />

      <ImportFieldsOfLaw
        :importable-fields-of-law="
          documentUnitToImport.contentRelatedIndexing.fieldsOfLaw ?? []
        "
      />
    </div>
  </div>
</template>
