<script setup lang="ts">
import dayjs from "dayjs"
import { ref } from "vue"
import { RouterLink } from "vue-router"
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
const documentUnit = ref<DocumentUnit | undefined>(undefined)
const errorMessage = ref<string | undefined>(undefined)

/**
 * Loads the document unit to import category data from.
 * Displays an error message, if no document unit could be found with
 * the given document number could be found.
 */
async function searchForDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    documentNumber.value,
  )
  if (response.data) {
    documentUnit.value = response.data
    errorMessage.value = undefined
  } else {
    documentUnit.value = undefined
    errorMessage.value = "Keine Dokumentationseinheit gefunden."
  }
}
</script>

<template>
  <div data-testid="category-import">
    <span class="ds-label-01-bold">Rubriken importieren</span>
    <div class="flex flex-row items-end gap-8">
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
      v-if="documentUnit"
      class="ds-label-02-reg mt-24 flex flex-col gap-16 bg-blue-100 p-16"
    >
      <RouterLink
        v-if="documentUnit.documentNumber"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: { documentNumber: documentUnit.documentNumber },
        }"
      >
        <LinkElement :title="documentUnit.documentNumber" />
      </RouterLink>

      <IconBadge v-bind="useStatusBadge(documentUnit.status).value" />

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900"> Gericht: </span>
        {{ documentUnit.coreData?.court?.label }}
      </div>

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900"> Aktenzeichen: </span>
        {{ documentUnit.coreData?.fileNumbers?.join(", ") }}
      </div>

      <div class="flex flex-col">
        <span class="ds-label-02-bold text-gray-900">Entscheidungsdatum</span>
        {{ dayjs(documentUnit.coreData?.decisionDate).format("DD.MM.YYYY") }}
      </div>
    </div>
  </div>
</template>
