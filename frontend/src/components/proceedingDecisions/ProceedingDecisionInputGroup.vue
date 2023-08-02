<script lang="ts" setup>
import { onMounted, ref } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import LinkedDocumentUnit from "@/domain/linkedDocumentUnit"
import ProceedingDecision from "@/domain/proceedingDecision"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{
  modelValue?: ProceedingDecision
  modelValueList?: ProceedingDecision[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: ProceedingDecision]
  addEntry: [void]
}>()

const proceedingDecision = ref(new ProceedingDecision({ ...props.modelValue }))
const validationErrors = ref<ValidationError[]>()
const searchRunning = ref(false)
const searchResultsCurrentPage = ref<Page<ProceedingDecision>>()
const searchResults = ref<SearchResults<ProceedingDecision>>()

async function search(page = 0) {
  const proceedingDecisionRef = new ProceedingDecision({
    ...proceedingDecision.value,
  })

  const response = await documentUnitService.searchByLinkedDocumentUnit(
    page,
    30,
    proceedingDecisionRef,
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new ProceedingDecision({ ...decision }),
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new ProceedingDecision({ ...searchResult }),
        isLinked: searchResult.isLinked(props.modelValueList),
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
  validationErrors.value = []
  if (proceedingDecision.value.missingRequiredFields?.length) {
    proceedingDecision.value.missingRequiredFields.forEach((missingField) => {
      validationErrors.value?.push({
        defaultMessage: "Pflichtfeld nicht befüllt",
        field: missingField,
      })
    })
  }
}

async function addProceedingDecision() {
  validateRequiredInput()
  emit("update:modelValue", proceedingDecision.value as ProceedingDecision)
  emit("addEntry")
}

async function addProceedingDecisionFromSearch(decision: LinkedDocumentUnit) {
  emit("update:modelValue", decision as ProceedingDecision)
  emit("addEntry")
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("proceedingDecisions")
  if (element) {
    const headerOffset = 170
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
  // ProceedingDecision and is therefore the inital load. As soons as we are using
  // uuids, the check should be 'props.modelValue?.uuid !== undefined'

  if (props.modelValue?.isEmpty !== undefined) {
    validateRequiredInput()
  }
  proceedingDecision.value = new ProceedingDecision({ ...props.modelValue })
})
</script>

<template>
  <div>
    <div class="flex justify-between gap-24">
      <InputField
        id="court"
        v-slot="slotProps"
        label="Gericht *"
        :validation-error="
          validationErrors?.find((err) => err.field === 'court')
        "
      >
        <ComboboxInput
          id="court"
          v-model="proceedingDecision.court"
          aria-label="Gericht Rechtszug"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getCourts"
          placeholder="Gerichtstyp Gerichtsort"
        ></ComboboxInput>
      </InputField>
      <div class="flex w-full justify-between gap-24">
        <InputField
          id="date"
          v-slot="slotProps"
          label="Entscheidungsdatum *"
          :validation-error="
            validationErrors?.find((err) => err.field === 'decisionDate')
          "
        >
          <DateInput
            id="decisionDate"
            v-model="proceedingDecision.decisionDate"
            aria-label="Entscheidungsdatum Rechtszug"
            clear-on-choosing-item
            :disabled="proceedingDecision.dateUnknown"
            :has-error="slotProps.hasError"
            @update:validation-error="slotProps.updateValidationError"
          ></DateInput>
        </InputField>
        <InputField
          id="regularCheckbox"
          v-slot="{ id }"
          label="Datum unbekannt"
        >
          <CheckboxInput
            :id="id"
            v-model="proceedingDecision.dateUnknown"
            aria-label="Datum Unbekannt Rechtszug"
          />
        </InputField>
      </div>
    </div>

    <div class="flex justify-between gap-24">
      <InputField
        id="fileNumber"
        v-slot="slotProps"
        class="fake-input-group__row__field flex-col"
        label="Aktenzeichen *"
        :validation-error="
          validationErrors?.find((err) => err.field === 'fileNumber')
        "
      >
        <TextInput
          id="fileNumber"
          v-model="proceedingDecision.fileNumber"
          aria-label="Aktenzeichen Rechtszug"
          :has-error="slotProps.hasError"
          placeholder="Aktenzeichen"
        ></TextInput>
      </InputField>

      <InputField
        id="documentType"
        class="fake-input-group__row__field flex-col"
        label="Dokumenttyp"
      >
        <ComboboxInput
          id="documentType"
          v-model="proceedingDecision.documentType"
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
        :disabled="proceedingDecision.isEmpty"
        label="Übernehmen"
        @click="addProceedingDecision"
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
          @link-decision="addProceedingDecisionFromSearch"
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
