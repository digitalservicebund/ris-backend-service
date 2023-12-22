<script lang="ts" setup>
import { onMounted, ref, computed } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
import PreviousDecision from "@/domain/previousDecision"
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
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(30)
const isLoading = ref(false)

const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

const dateUnkown = computed({
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
  const previousDecision = new PreviousDecision({
    ...decision,
    referenceFound: true,
  })
  emit("update:modelValue", previousDecision)
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
    <InputField
      id="dateKnown"
      v-slot="{ id }"
      label="Datum unbekannt"
      :label-position="LabelPosition.RIGHT"
    >
      <CheckboxInput
        :id="id"
        v-model="dateUnkown"
        aria-label="Datum Unbekannt Vorgehende Entscheidung"
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
          @click="validationStore.remove('court')"
        ></ComboboxInput>
      </InputField>
      <div v-if="!dateUnkown" class="flex w-full justify-between gap-24">
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
          aria-label="Aktenzeichen Vorgehende Entscheidung"
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
          v-model="previousDecision.documentType"
          aria-label="Dokumenttyp Vorgehende Entscheidung"
          :item-service="ComboboxItemService.getDocumentTypes"
        ></ComboboxInput>
      </InputField>
    </div>

    <div>
      <TextButton
        aria-label="Nach Entscheidung suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        size="small"
        @click="search"
      />
      <TextButton
        aria-label="Vorgehende Entscheidung speichern"
        class="mr-28"
        :disabled="previousDecision.isEmpty"
        label="Übernehmen"
        size="small"
        @click="addPreviousDecision"
      />
    </div>

    <div class="mb-10 mt-20">
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
