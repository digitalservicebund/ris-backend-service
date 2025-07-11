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
import NestedComponent from "@/components/NestedComponents.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { useIsSaved } from "@/composables/useIsSaved"
import { useScroll } from "@/composables/useScroll"
import { useValidationStore } from "@/composables/useValidationStore"
import PreviousDecision from "@/domain/previousDecision"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  modelValue?: PreviousDecision
  modelValueList?: PreviousDecision[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: PreviousDecision]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value: PreviousDecision]
}>()
const { scrollIntoViewportById } = useScroll()
const { isSaved } = useIsSaved(props.modelValue, props.modelValueList)
const lastSearchInput = ref(new PreviousDecision())
const lastSavedModelValue = ref(new PreviousDecision({ ...props.modelValue }))
const previousDecision = ref(new PreviousDecision({ ...props.modelValue }))
const validationStore =
  useValidationStore<(typeof PreviousDecision.fields)[number]>()

const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)

const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

const dateUnknown = computed({
  get: () => !previousDecision.value.dateKnown,
  set: (value) => {
    if (value) previousDecision.value.decisionDate = undefined
    previousDecision.value.dateKnown = !value
  },
})

async function search() {
  isLoading.value = true
  const previousDecisionRef = new PreviousDecision({
    ...previousDecision.value,
  })

  if (
    previousDecisionRef.court != lastSearchInput.value.court ||
    previousDecisionRef.decisionDate != lastSearchInput.value.decisionDate ||
    previousDecisionRef.fileNumber != lastSearchInput.value.fileNumber ||
    previousDecisionRef.documentType != lastSearchInput.value.documentType
  ) {
    pageNumber.value = 0
  }

  const urlParams = window.location.pathname.split("/")
  const documentNumberToExclude =
    urlParams[urlParams.indexOf("documentunit") + 1]

  const response = await documentUnitService.searchByRelatedDocumentation(
    previousDecisionRef,
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
  lastSearchInput.value = previousDecisionRef
  isLoading.value = false
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

async function validateRequiredInput() {
  validationStore.reset()
  if (previousDecision.value.missingRequiredFields?.length) {
    previousDecision.value.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

async function addPreviousDecision() {
  if (
    !validationStore.getByMessage("Kein valides Datum").length &&
    !validationStore.getByMessage("Unvollständiges Datum").length &&
    !validationStore.getByMessage("Das Datum darf nicht in der Zukunft liegen")
      .length
  ) {
    await validateRequiredInput()
    emit("update:modelValue", previousDecision.value as PreviousDecision)
    emit("addEntry")
  }
}

async function addPreviousDecisionFromSearch(decision: RelatedDocumentation) {
  previousDecision.value = new PreviousDecision({
    ...decision,
    deviatingFileNumber: previousDecision.value.deviatingFileNumber,
  })
  emit("update:modelValue", previousDecision.value as PreviousDecision)
  emit("addEntry")
  await scrollIntoViewportById("previousDecisions")
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
    previousDecision.value = new PreviousDecision({ ...props.modelValue })
    lastSavedModelValue.value = new PreviousDecision({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(async () => {
  // don't validate on initial empty entries
  if (isSaved.value) {
    await validateRequiredInput()
  }
  previousDecision.value = new PreviousDecision({ ...props.modelValue })
})
</script>

<template>
  <div v-ctrl-enter="search" class="flex flex-col gap-24">
    <div class="flex flex-col gap-24">
      <InputField
        id="dateKnown"
        v-slot="{ id }"
        label="Datum unbekannt"
        label-class="ris-label1-regular"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          v-model="dateUnknown"
          aria-label="Datum Unbekannt Vorgehende Entscheidung"
          binary
          :disabled="previousDecision.hasForeignSource"
          :input-id="id"
          size="large"
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
            v-model="previousDecision.court"
            aria-label="Gericht Vorgehende Entscheidung"
            clear-on-choosing-item
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getCourts"
            :read-only="previousDecision.hasForeignSource"
            @focus="validationStore.remove('court')"
          ></ComboboxInput>
        </InputField>
        <div v-if="!dateUnknown" class="flex w-full justify-between gap-24">
          <InputField
            id="date"
            v-slot="slotProps"
            label="Entscheidungsdatum *"
            :validation-error="validationStore.getByField('decisionDate')"
            @update:validation-error="
              (validationError: any) =>
                updateDateFormatValidation(validationError)
            "
          >
            <DateInput
              id="decisionDate"
              v-model="previousDecision.decisionDate"
              aria-label="Entscheidungsdatum Vorgehende Entscheidung"
              :has-error="slotProps.hasError"
              :readonly="previousDecision.hasForeignSource"
              @focus="validationStore.remove('decisionDate')"
              @update:validation-error="slotProps.updateValidationError"
            ></DateInput>
          </InputField>
        </div>
      </div>

      <div class="flex justify-between gap-24">
        <NestedComponent
          aria-label="Abweichendes Aktenzeichen Vorgehende Entscheidung"
          class="w-full"
          :is-open="
            previousDecision.hasForeignSource ||
            !!previousDecision.deviatingFileNumber
          "
        >
          <InputField
            id="fileNumber"
            v-slot="slotProps"
            class="flex-col"
            label="Aktenzeichen *"
            :validation-error="validationStore.getByField('fileNumber')"
          >
            <InputText
              id="fileNumber"
              v-model="previousDecision.fileNumber"
              aria-label="Aktenzeichen Vorgehende Entscheidung"
              fluid
              :invalid="slotProps.hasError"
              :readonly="previousDecision.hasForeignSource"
              size="small"
              @focus="validationStore.remove('fileNumber')"
            ></InputText>
          </InputField>
          <!-- Child  -->
          <template #children>
            <InputField
              id="deviatingFileNumber"
              v-slot="slotProps"
              class="flex-col"
              label="Abweichendes Aktenzeichen Vorinstanz"
              :validation-error="
                validationStore.getByField('deviatingFileNumber')
              "
            >
              <InputText
                id="deviatingFileNumber"
                v-model="previousDecision.deviatingFileNumber"
                aria-label="Abweichendes Aktenzeichen Vorgehende Entscheidung"
                fluid
                :invalid="slotProps.hasError"
                size="small"
                @input="validationStore.remove('deviatingFileNumber')"
              ></InputText>
            </InputField>
          </template>
        </NestedComponent>

        <InputField id="documentType" class="flex-col" label="Dokumenttyp">
          <ComboboxInput
            id="documentType"
            v-model="previousDecision.documentType"
            aria-label="Dokumenttyp Vorgehende Entscheidung"
            :item-service="ComboboxItemService.getCaselawDocumentTypes"
            :read-only="previousDecision.hasForeignSource"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            v-if="!modelValue?.hasForeignSource"
            aria-label="Nach Entscheidung suchen"
            label="Suchen"
            size="small"
            @click="search"
          ></Button>
          <Button
            aria-label="Vorgehende Entscheidung speichern"
            data-testid="previous-decision-save-button"
            :disabled="previousDecision.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addPreviousDecision"
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
        @click.stop="modelValue && emit('removeEntry', modelValue)"
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
          @link-decision="addPreviousDecisionFromSearch"
        />
      </Pagination>
    </div>
  </div>
</template>
