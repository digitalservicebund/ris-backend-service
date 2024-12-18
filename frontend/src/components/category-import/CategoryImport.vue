<script setup lang="ts">
import { ref, toRaw, computed } from "vue"
import { RouterLink } from "vue-router"
import ImportListCategories from "@/components/category-import/ImportListCategories.vue"
import IconBadge from "@/components/IconBadge.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconSearch from "~icons/ic/baseline-search"

const documentNumber = ref("")
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
        {{ documentUnitToImport.renderDecision }}
        <IconBadge
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

      <ImportListCategories
        :importable-fields-of-law="
          documentUnitToImport.contentRelatedIndexing.fieldsOfLaw
        "
        :importable-keywords="
          documentUnitToImport.contentRelatedIndexing.keywords
        "
      />
    </div>
  </div>
</template>
