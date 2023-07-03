<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import SearchResultList, {
  SearchResults,
} from "./proceedingDecisions/SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ActiveCitation from "@/domain/activeCitation"
import { CitationStyle } from "@/domain/citationStyle"
import LinkedDocumentUnit from "@/domain/linkedDocumentUnit"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

interface Emits {
  (event: "update:modelValue", value: ActiveCitation): void
  (event: "closeEntry"): void
}

const props = defineProps<{
  modelValue?: ActiveCitation
  modelValueList?: ActiveCitation[]
}>()
const emit = defineEmits<Emits>()
const activeCitation = ref(props.modelValue as ActiveCitation)
const validationErrors = ref<ValidationError[]>()
const searchRunning = ref(false)

const activeCitationStyle = computed({
  get: () =>
    activeCitation?.value?.citationStyle
      ? {
          label: activeCitation.value.citationStyle.label,
          value: activeCitation.value.citationStyle,
          additionalInformation:
            activeCitation.value.citationStyle.jurisShortcut,
        }
      : undefined,
  set: (newValue) => {
    const newActiveCitationStyle = { ...newValue } as CitationStyle
    const activeCitationRef = new ActiveCitation({
      ...activeCitation.value,
      citationStyle: newActiveCitationStyle,
    })
    activeCitation.value = activeCitationRef
  },
})

const searchResultsCurrentPage = ref<Page<ActiveCitation>>()
const searchResults = ref<SearchResults<ActiveCitation>>()

async function search(page = 0) {
  const activeCitationRef = new ActiveCitation({
    ...activeCitation.value,
  })

  if (activeCitationRef.citationStyle) {
    delete activeCitationRef["citationStyle"]
  }
  const response = await documentUnitService.searchByLinkedDocumentUnit(
    page,
    30,
    activeCitationRef as ActiveCitation
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new ActiveCitation({ ...decision })
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new ActiveCitation({ ...searchResult }),
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

async function validateRequiredInput(citation: ActiveCitation) {
  validationErrors.value = []
  if (citation.missingRequiredFields?.length) {
    citation.missingRequiredFields.forEach((missingField) => {
      validationErrors.value?.push({
        defaultMessage: "Pflichtfeld nicht befüllt",
        field: missingField,
      })
    })
  }
}

async function addActiveCitation() {
  const citation = new ActiveCitation({ ...activeCitation.value })
  validateRequiredInput(citation)
  emit("update:modelValue", citation)
  emit("closeEntry")
}

async function addActiveCitationFromSearch(decision: LinkedDocumentUnit) {
  const newActiveCitationStyle = {
    ...activeCitationStyle.value?.value,
  } as CitationStyle
  const decisionWithCitationStyle = new ActiveCitation({
    ...decision,
    citationStyle: newActiveCitationStyle,
  })
  emit("update:modelValue", decisionWithCitationStyle)
  emit("closeEntry")
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("activeCitations")
  if (element) {
    const headerOffset = 170
    const elementPosition = element?.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: "smooth",
    })
  }
}

onMounted(() => {
  activeCitation.value = props.modelValue as ActiveCitation
  validateRequiredInput(activeCitation.value as ActiveCitation)
})
</script>

<template>
  <div>
    <InputField
      id="activeCitationPredicate"
      v-slot="slotProps"
      class="border-b-1 border-gray-400 mb-16"
      label="Art der Zitierung *"
      :validation-error="
        validationErrors?.find((err) => err.field === 'citationStyle')
          ?.defaultMessage
      "
    >
      <ComboboxInput
        id="activeCitationPredicate"
        v-model="activeCitationStyle"
        aria-label="Art der Zitierung"
        clear-on-choosing-item
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getCitationStyles"
        placeholder="Bitte auswählen"
      ></ComboboxInput>
    </InputField>
    <div class="flex gap-24 justify-between">
      <InputField
        id="activeCitationCourt"
        v-slot="slotProps"
        label="Gericht *"
        :validation-error="
          validationErrors?.find((err) => err.field === 'court')?.defaultMessage
        "
      >
        <ComboboxInput
          id="activeCitationCourt"
          v-model="activeCitation.court"
          aria-label="Gericht Aktivzitierung"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getCourts"
          placeholder="Aktivzitierung Gericht"
        >
        </ComboboxInput>
      </InputField>
      <InputField
        id="activeCitationDecisionDate"
        v-slot="slotProps"
        label="Entscheidungsdatum *"
        :validation-error="
          validationErrors?.find((err) => err.field === 'decisionDate')
            ?.defaultMessage
        "
      >
        <DateInput
          id="activeCitationDecisionDate"
          v-model="activeCitation.decisionDate"
          aria-label="Entscheidungsdatum Aktivzitierung"
          :has-error="slotProps.hasError"
        ></DateInput>
      </InputField>
    </div>
    <div class="flex gap-24 justify-between">
      <InputField
        id="activeCitationFileNumber"
        v-slot="slotProps"
        label="Aktenzeichen"
        :validation-error="
          validationErrors?.find((err) => err.field === 'fileNumber')
            ?.defaultMessage
        "
      >
        <TextInput
          id="activeCitationDocumentType"
          v-model="activeCitation.fileNumber"
          aria-label="Aktenzeichen Aktivzitierung"
          :has-error="slotProps.hasError"
          placeholder="Aktenzeichen *"
        ></TextInput>
      </InputField>
      <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
        <ComboboxInput
          id="activeCitationDecisionDocumentType"
          v-model="activeCitation.documentType"
          aria-label="Dokumenttyp Aktivzitierung"
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
        aria-label="Aktivzitierung speichern"
        class="mr-28"
        label="Übernehmen"
        @click="addActiveCitation"
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
          @link-decision="addActiveCitationFromSearch"
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
