<script lang="ts" setup>
import { watch, ref, computed, onMounted, onBeforeUnmount } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import NestedComponent from "@/components/NestedComponents.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
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
  removeListEntry: [void]
}>()

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

  const response = await documentUnitService.searchByRelatedDocumentation(
    pageNumber.value,
    itemsPerPage.value,
    previousDecisionRef,
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
  search()
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
  validateRequiredInput()
  emit("update:modelValue", previousDecision.value as PreviousDecision)
  emit("addEntry")
}

async function addPreviousDecisionFromSearch(decision: RelatedDocumentation) {
  const newPreviousDecision = new PreviousDecision({
    ...decision,
    referenceFound: true,
    deviatingFileNumber: previousDecision.value.deviatingFileNumber,
  })

  emit("update:modelValue", newPreviousDecision)
  emit("addEntry")
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("previousDecisions")
  if (element) {
    const headerOffset = values.headerOffset
    const elementPosition = element?.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.scrollY - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: "smooth",
    })
  }
}

watch(
  () => props.modelValue,
  () => {
    previousDecision.value = new PreviousDecision({ ...props.modelValue })
    lastSavedModelValue.value = new PreviousDecision({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  // On first mount, we don't need to validate. When the props.modelValue do not
  // have the isEmpty getter, we can be sure that it has not been initialized as
  // PreviousDecision and is therefore the inital load. As soons as we are using
  // uuids, the check should be 'props.modelValue?.uuid !== undefined'

  if (props.modelValue?.isEmpty !== undefined) {
    validateRequiredInput()
  }
  previousDecision.value = new PreviousDecision({ ...props.modelValue })
})

onBeforeUnmount(() => {
  if (previousDecision.value.isEmpty) emit("removeListEntry")
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-col gap-24">
      <InputField
        id="dateKnown"
        v-slot="{ id }"
        label="Datum unbekannt"
        label-class="ds-label-01-reg"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="id"
          v-model="dateUnknown"
          aria-label="Datum Unbekannt Vorgehende Entscheidung"
          :readonly="previousDecision.hasForeignSource"
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
            v-model="previousDecision.court"
            aria-label="Gericht Vorgehende Entscheidung"
            clear-on-choosing-item
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getCourts"
            :readonly="previousDecision.hasForeignSource"
            @click="validationStore.remove('court')"
          ></ComboboxInput>
        </InputField>
        <div v-if="!dateUnknown" class="flex w-full justify-between gap-24">
          <InputField
            id="date"
            v-slot="slotProps"
            label="Entscheidungsdatum *"
            :validation-error="validationStore.getByField('decisionDate')"
          >
            <DateInput
              id="decisionDate"
              v-model="previousDecision.decisionDate"
              aria-label="Entscheidungsdatum Vorgehende Entscheidung"
              class="ds-input-medium"
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
          :is-open="previousDecision.hasForeignSource"
        >
          <InputField
            id="fileNumber"
            v-slot="slotProps"
            class="flex-col"
            label="Aktenzeichen *"
            :validation-error="validationStore.getByField('fileNumber')"
          >
            <TextInput
              id="fileNumber"
              v-model="previousDecision.fileNumber"
              aria-label="Aktenzeichen Vorgehende Entscheidung"
              class="ds-input-medium"
              :has-error="slotProps.hasError"
              :readonly="previousDecision.hasForeignSource"
              size="medium"
              @input="validationStore.remove('fileNumber')"
            ></TextInput>
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
              <TextInput
                id="deviatingFileNumber"
                v-model="previousDecision.deviatingFileNumber"
                aria-label="Abweichendes Aktenzeichen Vorgehende Entscheidung"
                class="ds-input-medium"
                :has-error="slotProps.hasError"
                size="medium"
                @input="validationStore.remove('deviatingFileNumber')"
              ></TextInput>
            </InputField>
          </template>
        </NestedComponent>

        <InputField id="documentType" class="flex-col" label="Dokumenttyp">
          <ComboboxInput
            id="documentType"
            v-model="previousDecision.documentType"
            aria-label="Dokumenttyp Vorgehende Entscheidung"
            :item-service="ComboboxItemService.getDocumentTypes"
            :readonly="previousDecision.hasForeignSource"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <TextButton
            v-if="!modelValue?.hasForeignSource"
            aria-label="Nach Entscheidung suchen"
            button-type="primary"
            label="Suchen"
            size="small"
            @click="search"
          />
          <TextButton
            aria-label="Vorgehende Entscheidung speichern"
            button-type="tertiary"
            :disabled="previousDecision.isEmpty"
            label="Übernehmen"
            size="small"
            @click.stop="addPreviousDecision"
          />
          <TextButton
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="emit('cancelEdit')"
          />
        </div>
      </div>
      <TextButton
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        button-type="destructive"
        label="Eintrag löschen"
        size="small"
        @click.stop="emit('removeListEntry')"
      />
    </div>

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
</template>
