<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
import EnsuingDecision from "@/domain/ensuingDecision"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{
  modelValue?: EnsuingDecision
  modelValueList?: EnsuingDecision[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: EnsuingDecision]
  addEntry: [void]
}>()

const ensuingDecision = ref(new EnsuingDecision({ ...props.modelValue }))
const validationStore =
  useValidationStore<(typeof EnsuingDecision.fields)[number]>()
const searchRunning = ref(false)
const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

const isPending = computed({
  get: () => ensuingDecision.value.pending,
  set: (value) => {
    if (value) ensuingDecision.value.decisionDate = undefined
    ensuingDecision.value.pending = value
  },
})

async function search(page = 0) {
  const ensuingDecisionRef = new EnsuingDecision({
    ...ensuingDecision.value,
  })

  const response = await documentUnitService.searchByRelatedDocumentation(
    page,
    30,
    ensuingDecisionRef,
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
  searchRunning.value = false
}

function handleSearch() {
  searchRunning.value = true
  search(0)
}

async function validateRequiredInput() {
  validationStore.reset()
  if (ensuingDecision.value.missingRequiredFields?.length) {
    ensuingDecision.value.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

async function addEnsuingDecision() {
  validateRequiredInput()
  emit("update:modelValue", ensuingDecision.value as EnsuingDecision)
  emit("addEntry")
}

async function addEnsuingDecisionFromSearch(decision: RelatedDocumentation) {
  const decisionWithNote = new EnsuingDecision({
    ...decision,
    pending: ensuingDecision.value?.pending,
    referenceFound: true,
    note: ensuingDecision.value?.note,
  })
  emit("update:modelValue", decisionWithNote)
  emit("addEntry")
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("ensuingDecisions")
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
  // EnsuingDecision and is therefore the inital load. As soons as we are using
  // uuids, the check should be 'props.modelValue?.uuid !== undefined'

  if (props.modelValue?.isEmpty !== undefined) {
    validateRequiredInput()
  }
  ensuingDecision.value = new EnsuingDecision({ ...props.modelValue })
})
</script>

<template>
  <div>
    <div v-if="!ensuingDecision.hasForeignSource">
      <InputField
        id="isPending"
        v-slot="{ id }"
        label="anhängig"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="id"
          v-model="isPending"
          aria-label="Anhängige Entscheidung"
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
            clear-on-choosing-item
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getCourts"
            @click="validationStore.remove('court')"
          ></ComboboxInput>
        </InputField>
        <div v-if="!isPending" class="flex w-full justify-between gap-24">
          <InputField
            id="date"
            v-slot="slotProps"
            label="Entscheidungsdatum *"
            :validation-error="validationStore.getByField('decisionDate')"
          >
            <DateInput
              id="decisionDate"
              v-model="ensuingDecision.decisionDate"
              aria-label="Entscheidungsdatum Nachgehende Entscheidung"
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
            v-model="ensuingDecision.fileNumber"
            aria-label="Aktenzeichen Nachgehende Entscheidung"
            :has-error="slotProps.hasError"
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
            v-model="ensuingDecision.documentType"
            aria-label="Dokumenttyp Nachgehende Entscheidung"
            :item-service="ComboboxItemService.getDocumentTypes"
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
      <TextInput
        :id="id"
        v-model="ensuingDecision.note"
        aria-label="Vermerk"
        :has-error="hasError"
        @input="validationStore.remove('note')"
      ></TextInput>
    </InputField>

    <div>
      <TextButton
        v-if="!ensuingDecision.hasForeignSource"
        aria-label="Nach Entscheidung suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        size="small"
        @click="handleSearch"
      />
      <TextButton
        aria-label="Nachgehende Entscheidung speichern"
        class="mr-28"
        :disabled="ensuingDecision.isEmpty"
        label="Übernehmen"
        size="small"
        @click="addEnsuingDecision"
      />
    </div>

    <div v-if="!searchRunning && searchResultsCurrentPage" class="mb-10 mt-20">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="search"
      >
        <SearchResultList
          :search-results="searchResults"
          @link-decision="addEnsuingDecisionFromSearch"
        />
      </Pagination>
    </div>
    <div v-if="searchRunning" class="mb-10 ml-40 mt-20">
      ... Suche läuft ...
    </div>
  </div>
</template>
