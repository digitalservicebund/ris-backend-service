<script lang="ts" setup>
import Button from "primevue/button"
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import { computed, onMounted, ref, watch } from "vue"
import { ValidationError } from "./input/types"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { useIsSaved } from "@/composables/useIsSaved"
import { useScroll } from "@/composables/useScroll"
import { useValidationStore } from "@/composables/useValidationStore"
import EnsuingDecision from "@/domain/ensuingDecision"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemServices from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  modelValue?: EnsuingDecision
  modelValueList?: EnsuingDecision[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: EnsuingDecision]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const { scrollIntoViewportById } = useScroll()
const { isSaved } = useIsSaved(props.modelValue, props.modelValueList)
const lastSearchInput = ref(new EnsuingDecision())
const lastSavedModelValue = ref(new EnsuingDecision({ ...props.modelValue }))
const ensuingDecision = ref(new EnsuingDecision({ ...props.modelValue }))
const validationStore =
  useValidationStore<(typeof EnsuingDecision.fields)[number]>()
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)
const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

const isPending = computed({
  get: () => ensuingDecision.value.pending,
  set: (value) => {
    if (value) ensuingDecision.value.decisionDate = undefined
    ensuingDecision.value.pending = value
  },
})

async function search() {
  isLoading.value = true
  const ensuingDecisionRef = new EnsuingDecision({
    ...ensuingDecision.value,
  })

  if (
    ensuingDecisionRef.court != lastSearchInput.value.court ||
    ensuingDecisionRef.decisionDate != lastSearchInput.value.decisionDate ||
    ensuingDecisionRef.fileNumber != lastSearchInput.value.fileNumber ||
    ensuingDecisionRef.documentType != lastSearchInput.value.documentType
  ) {
    pageNumber.value = 0
  }

  const urlParams = window.location.pathname.split("/")
  const documentNumberToExclude =
    urlParams[urlParams.indexOf("documentunit") + 1]

  const response = await documentUnitService.searchByRelatedDocumentation(
    ensuingDecisionRef,
    {
      ...(pageNumber.value != undefined
        ? { pg: pageNumber.value.toString() }
        : {}),
      ...(itemsPerPage.value != undefined
        ? { sz: itemsPerPage.value.toString() }
        : {}),
      ...(documentNumberToExclude != undefined
        ? { documentNumber: documentNumberToExclude.toString() }
        : {}),
    },
  )

  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new RelatedDocumentation({ ...decision }),
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new RelatedDocumentation({ ...searchResult }),
        isLinked: searchResult.isLinkedWith(props.modelValueList),
      }
    })
  }
  isLoading.value = false
  lastSearchInput.value = ensuingDecisionRef
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

function validateRequiredInput() {
  validationStore.reset()
  if (ensuingDecision.value.missingRequiredFields?.length) {
    ensuingDecision.value.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

function addEnsuingDecision() {
  if (
    !validationStore.getByMessage("Kein valides Datum").length &&
    !validationStore.getByMessage("Unvollständiges Datum").length &&
    !validationStore.getByMessage("Das Datum darf nicht in der Zukunft liegen")
      .length
  ) {
    validateRequiredInput()
    emit("update:modelValue", ensuingDecision.value as EnsuingDecision)
    emit("addEntry")
  }
}

async function addEnsuingDecisionFromSearch(decision: RelatedDocumentation) {
  ensuingDecision.value = new EnsuingDecision({
    ...decision,
    pending: ensuingDecision.value?.pending,
    note: ensuingDecision.value?.note,
  })
  emit("update:modelValue", ensuingDecision.value as EnsuingDecision)
  emit("addEntry")
  await scrollIntoViewportById("ensuingDecisions")
}

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
) {
  if (validationError)
    validationStore.add(validationError.message, "decisionDate")
  else validationStore.remove("decisionDate")
}

watch(
  () => props.modelValue,
  () => {
    ensuingDecision.value = new EnsuingDecision({ ...props.modelValue })
    lastSavedModelValue.value = new EnsuingDecision({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  // we don't want to validate on initial empty entries
  if (isSaved.value) {
    validateRequiredInput()
  }
  ensuingDecision.value = new EnsuingDecision({ ...props.modelValue })
})
</script>

<template>
  <div v-ctrl-enter="search" class="flex flex-col gap-24">
    <div class="flex flex-col gap-24">
      <InputField
        id="isPending"
        v-slot="{ id }"
        label="Anhängig"
        label-class="ris-label1-regular"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          v-model="isPending"
          aria-label="Anhängige Entscheidung"
          binary
          :disabled="ensuingDecision.hasForeignSource"
          :input-id="id"
          size="small"
        />
      </InputField>
      <div class="flex justify-between gap-24">
        <InputField
          id="court"
          v-slot="slotProps"
          label="Gericht *"
          :validation-error="validationStore.getByField('court')"
        >
          <ComboboxInput
            id="court"
            v-model="ensuingDecision.court"
            aria-label="Gericht Nachgehende Entscheidung"
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemServices.getCourts"
            :read-only="ensuingDecision.hasForeignSource"
            @show="validationStore.remove('court')"
          ></ComboboxInput>
        </InputField>
        <div v-if="!isPending" class="flex w-full justify-between gap-24">
          <InputField
            id="date"
            v-slot="slotProps"
            label="Entscheidungsdatum *"
            :validation-error="validationStore.getByField('decisionDate')"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                updateDateFormatValidation(validationError)
            "
          >
            <DateInput
              id="decisionDate"
              v-model="ensuingDecision.decisionDate"
              aria-label="Entscheidungsdatum Nachgehende Entscheidung"
              :has-error="slotProps.hasError"
              :readonly="ensuingDecision.hasForeignSource"
              @focus="validationStore.remove('decisionDate')"
              @update:validation-error="slotProps.updateValidationError"
            ></DateInput>
          </InputField>
        </div>
      </div>

      <div class="flex justify-between gap-24">
        <InputField
          id="fileNumber"
          v-slot="slotProps"
          class="fake-input-group__row__field flex-col"
          label="Aktenzeichen *"
          :validation-error="validationStore.getByField('fileNumber')"
        >
          <InputText
            id="fileNumber"
            v-model="ensuingDecision.fileNumber"
            aria-label="Aktenzeichen Nachgehende Entscheidung"
            fluid
            :invalid="slotProps.hasError"
            :readonly="ensuingDecision.hasForeignSource"
            size="small"
            @focus="validationStore.remove('fileNumber')"
          ></InputText>
        </InputField>

        <InputField
          id="documentType"
          class="fake-input-group__row__field flex-col"
          label="Dokumenttyp"
        >
          <ComboboxInput
            id="documentType"
            v-model="ensuingDecision.documentType"
            aria-label="Dokumenttyp Nachgehende Entscheidung"
            :item-service="ComboboxItemServices.getCaselawDocumentTypes"
            :read-only="ensuingDecision.hasForeignSource"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <InputField
      id="note"
      v-slot="{ id, hasError }"
      class="fake-input-group__row__field flex-col"
      label="Vermerk"
      :validation-error="validationStore.getByField('note')"
    >
      <InputText
        :id="id"
        v-model="ensuingDecision.note"
        aria-label="Vermerk"
        fluid
        :invalid="hasError"
        size="small"
        @input="validationStore.remove('note')"
      ></InputText>
    </InputField>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            v-if="!ensuingDecision.hasForeignSource"
            aria-label="Nach Entscheidung suchen"
            label="Suchen"
            size="small"
            @click="search"
          ></Button>
          <Button
            aria-label="Nachgehende Entscheidung speichern"
            :disabled="ensuingDecision.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addEnsuingDecision"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>

    <div v-if="isLoading || searchResults" class="bg-blue-200">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="updatePage"
      >
        <SearchResultList
          :is-loading="isLoading"
          :search-results="searchResults"
          @link-decision="addEnsuingDecisionFromSearch"
        />
      </Pagination>
    </div>
  </div>
</template>
