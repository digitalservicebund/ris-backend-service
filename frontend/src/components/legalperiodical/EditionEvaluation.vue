<script lang="ts" setup>
import { UUID } from "crypto"
import { onMounted, ref, computed } from "vue"
import { useRoute } from "vue-router"
import { ValidationError } from "../input/types"
import { Page } from "../Pagination.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import ErrorPage from "@/components/PageError.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { Court, DocumentType } from "@/domain/documentUnit"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import Reference from "@/domain/reference"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

type DocumentUnitSearchParameter = {
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
}

const fields = ["legalPeriodical", "citation", "referenceSupplement"]
const route = useRoute()
const edition = ref<LegalPeriodicalEdition>(new LegalPeriodicalEdition())
const reference = ref<Reference>(new Reference())
const documentationUnitSearchInput = ref<DocumentUnitSearchParameter>({})
const searchValidationStore = useValidationStore<(typeof fields)[number]>()
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)
const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()

const responseError = ref<ResponseError>()

const title = computed(
  () =>
    `Periodikaauswertung | ${edition.value.legalPeriodical?.abbreviation}, ${edition.value.name ? edition.value.name : edition.value.prefix}`,
)

const legalPeriodical = computed({
  get: () =>
    edition?.value?.legalPeriodical
      ? {
          label: edition?.value?.legalPeriodical.abbreviation,
          value: edition?.value?.legalPeriodical,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      edition.value.legalPeriodical = legalPeriodical
    } else {
      edition.value.legalPeriodical = undefined
    }
  },
})

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
) {
  if (validationError)
    searchValidationStore.add(validationError.message, "decisionDate")
  else searchValidationStore.remove("decisionDate")
}

async function search() {
  isLoading.value = true
  const documentationUnitRef = new RelatedDocumentation({
    ...documentationUnitSearchInput.value,
  })

  const response = await documentUnitService.searchByRelatedDocumentation(
    pageNumber.value,
    itemsPerPage.value,
    documentationUnitRef,
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new RelatedDocumentation({ ...decision }),
      ),
    }
  }
  isLoading.value = false
}

onMounted(async () => {
  const uuid = route.params.uuid
  if (uuid) {
    const response = await LegalPeriodicalEditionService.get(
      route.params.uuid.toString() as UUID,
    )
    if (response.error) {
      responseError.value = response.error
    }
    if (response.data) edition.value = response.data
  }
})
</script>

<template>
  <div
    v-if="!responseError"
    class="flex h-full flex-col space-y-24 px-16 py-16"
  >
    <h2 class="ds-heading-03-reg">{{ title }}</h2>

    <div v-if="edition" class="flex flex-col gap-24">
      <InputField id="legalPeriodical" label="Periodikum *">
        <ComboboxInput
          id="legalPeriodical"
          v-model="legalPeriodical"
          aria-label="Periodikum"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getLegalPeriodicals"
          read-only
        ></ComboboxInput>
      </InputField>
      <div class="flex flex-col gap-24">
        <div class="flex justify-between gap-24">
          <div class="flex-1">
            <InputField id="citation" label="Zitatstelle *">
              <div class="flex flex-row gap-4">
                <TextInput
                  id="citation prefix"
                  v-model="edition.prefix"
                  aria-label="Zitatstelle Präfix"
                  size="medium"
                ></TextInput>
                <TextInput
                  id="citation"
                  v-model="reference.citation"
                  aria-label="Zitatstelle *"
                  size="medium"
                ></TextInput>
                <TextInput
                  id="citation suffix"
                  v-model="edition.suffix"
                  aria-label="Zitatstelle Suffix"
                  size="medium"
                ></TextInput>
              </div>
            </InputField>

            <span v-if="legalPeriodical" class="ds-label-03-reg"
              >Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}</span
            >
          </div>
          <InputField
            id="citation"
            v-slot="slotProps"
            class="flex-1"
            label="Klammernzusatz"
          >
            <TextInput
              id="citation"
              v-model="reference.referenceSupplement"
              aria-label="Klammernzusatz"
              :has-error="slotProps.hasError"
              size="medium"
            ></TextInput>
          </InputField>
        </div>
      </div>
    </div>
    <div class="flex flex-col gap-24">
      <div class="flex justify-between gap-24">
        <InputField
          id="courtInput"
          v-slot="slotProps"
          label="Gericht *"
          :validation-error="searchValidationStore.getByField('court')"
        >
          <ComboboxInput
            id="courtInput"
            v-model="documentationUnitSearchInput.court"
            aria-label="Gericht Aktivzitierung"
            clear-on-choosing-item
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getCourts"
            @focus="searchValidationStore.remove('court')"
          >
          </ComboboxInput>
        </InputField>
        <InputField
          id="decisionDate"
          v-slot="slotProps"
          label="Entscheidungsdatum *"
          :validation-error="searchValidationStore.getByField('decisionDate')"
          @update:validation-error="
            (validationError) => updateDateFormatValidation(validationError)
          "
        >
          <DateInput
            id="decisionDate"
            v-model="documentationUnitSearchInput.decisionDate"
            aria-label="Entscheidungsdatum"
            class="ds-input-medium"
            :has-error="slotProps.hasError"
            @focus="searchValidationStore.remove('decisionDate')"
            @update:validation-error="slotProps.updateValidationError"
          ></DateInput>
        </InputField>
      </div>
      <div class="flex justify-between gap-24">
        <InputField
          id="activeCitationFileNumber"
          v-slot="slotProps"
          label="Aktenzeichen *"
          :validation-error="searchValidationStore.getByField('fileNumber')"
        >
          <TextInput
            id="activeCitationDocumentType"
            v-model="documentationUnitSearchInput.fileNumber"
            aria-label="Aktenzeichen Aktivzitierung"
            :has-error="slotProps.hasError"
            size="medium"
            @focus="searchValidationStore.remove('fileNumber')"
          ></TextInput>
        </InputField>
        <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
          <ComboboxInput
            id="activeCitationDecisionDocumentType"
            v-model="documentationUnitSearchInput.documentType"
            aria-label="Dokumenttyp Aktivzitierung"
            :item-service="ComboboxItemService.getDocumentTypes"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <TextButton
            aria-label="Nach Entscheidung suchen"
            button-type="primary"
            label="Suchen"
            size="small"
            @click="search"
          />
          <!-- <TextButton
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="emit('cancelEdit')"
          /> -->
        </div>
      </div>
      <!-- <TextButton
        aria-label="Eintrag löschen"
        button-type="destructive"
        label="Eintrag löschen"
        size="small"
        @click.stop="emit('removeEntry', true)"
      /> -->
    </div>
  </div>
  <ErrorPage
    v-else
    back-button-label="Zurück zur Übersicht"
    :back-router="{ name: 'caselaw-legal-periodical-editions' }"
    :error="responseError"
    :title="responseError?.title"
  />
</template>
