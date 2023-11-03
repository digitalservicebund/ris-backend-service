<script lang="ts" setup>
import { onMounted, ref } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
import LinkedDocumentUnit from "@/domain/linkedDocumentUnit"
import PreviousDecision from "@/domain/previousDecision"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{
  modelValue?: PreviousDecision
  modelValueList?: PreviousDecision[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: PreviousDecision]
  addEntry: [void]
}>()

const previousDecision = ref(new PreviousDecision({ ...props.modelValue }))
const validationStore =
  useValidationStore<(typeof PreviousDecision.fields)[number]>()
const searchRunning = ref(false)
const searchResultsCurrentPage = ref<Page<PreviousDecision>>()
const searchResults = ref<SearchResults<PreviousDecision>>()

async function search(page = 0) {
  const previousDecisionRef = new PreviousDecision({
    ...previousDecision.value,
  })

  const response = await documentUnitService.searchByLinkedDocumentUnit(
    page,
    30,
    previousDecisionRef,
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new PreviousDecision({ ...decision }),
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new PreviousDecision({ ...searchResult }),
        isLinked: searchResult.isLinkedWith(props.modelValueList),
      }
    })
  }
  searchRunning.value = false
}

function handleSearch() {
  searchRunning.value = true
  search(0)
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

async function addPreviousDecisionFromSearch(decision: LinkedDocumentUnit) {
  emit("update:modelValue", decision as PreviousDecision)
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
</script>

<template>
  <div>
    <InputField id="regularCheckbox" v-slot="{ id }" label="Datum unbekannt">
      <CheckboxInput
        :id="id"
        v-model="previousDecision.dateUnknown"
        aria-label="Datum Unbekannt Rechtszug"
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
          aria-label="Gericht Rechtszug"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getCourts"
          placeholder="Gerichtstyp Gerichtsort"
          @click="validationStore.remove('court')"
        ></ComboboxInput>
      </InputField>
      <div class="flex w-full justify-between gap-24">
        <InputField
          id="date"
          v-slot="slotProps"
          label="Entscheidungsdatum *"
          :validation-error="validationStore.getByField('decisionDate')"
        >
          <DateInput
            id="decisionDate"
            v-model="previousDecision.decisionDate"
            aria-label="Entscheidungsdatum Rechtszug"
            :disabled="previousDecision.dateUnknown"
            :has-error="slotProps.hasError"
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
        <TextInput
          id="fileNumber"
          v-model="previousDecision.fileNumber"
          aria-label="Aktenzeichen Rechtszug"
          :has-error="slotProps.hasError"
          placeholder="Aktenzeichen"
          @input="validationStore.remove('fileNumber')"
        ></TextInput>
      </InputField>

      <InputField
        id="documentType"
        class="fake-input-group__row__field flex-col"
        label="Dokumenttyp"
      >
        <ComboboxInput
          id="documentType"
          v-model="previousDecision.documentType"
          aria-label="Dokumenttyp Rechtszug"
          :item-service="ComboboxItemService.getDocumentTypes"
          placeholder="Bitte auswählen"
        ></ComboboxInput>
      </InputField>
    </div>

    <div>
      <TextButton
        aria-label="Nach Entscheidung suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        @click="handleSearch"
      />
      <TextButton
        aria-label="Vorgehende Entscheidung speichern"
        class="mr-28"
        :disabled="previousDecision.isEmpty"
        label="Übernehmen"
        @click="addPreviousDecision"
      />
    </div>

    <div v-if="searchResultsCurrentPage" class="mb-10 mt-20">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="search"
      >
        <SearchResultList
          :search-results="searchResults"
          @link-decision="addPreviousDecisionFromSearch"
        />
      </Pagination>
    </div>
    <div
      v-if="searchRunning && !searchResultsCurrentPage"
      class="mb-10 ml-40 mt-20"
    >
      ... Suche läuft ...
    </div>
  </div>
</template>
