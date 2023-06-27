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
}>()
const emit = defineEmits<Emits>()
const activeCitation = ref(props.modelValue as ActiveCitation)
const validationErrors = ref<ValidationError[]>()

const activeCitationPredicate = computed({
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

const addActiveCitationFromSearch = (decision: LinkedDocumentUnit) => {
  emit("update:modelValue", new ActiveCitation({ ...decision }))
  emit("closeEntry")
}

const searchResultsCurrentPage = ref<Page<ActiveCitation>>()
const searchResults = ref<SearchResults<ActiveCitation>>()
const localActiveCitations = ref<ActiveCitation[]>()

async function search(page = 0) {
  const response = await documentUnitService.searchByLinkedDocumentUnit(
    page,
    30,
    activeCitation.value as ActiveCitation
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
        isLinked: searchResult.isLinked(localActiveCitations.value),
      }
    })
  }
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

onMounted(() => {
  activeCitation.value = props.modelValue as ActiveCitation
  validateRequiredInput(activeCitation.value as ActiveCitation)
})
</script>

<template>
  <div>
    <InputField
      id="activeCitationPredicate"
      class="border-b-1 border-gray-400 mb-16"
      :error-message="
        validationErrors?.find((err) => err.field === 'citationStyle')
          ?.defaultMessage
      "
      label="Art der Zitierung"
    >
      <ComboboxInput
        id="activeCitationPredicate"
        v-model="activeCitationPredicate"
        aria-label="Art der Zitierung"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getCitationStyles"
        placeholder="Bitte auswählen"
      ></ComboboxInput>
    </InputField>
    <div class="flex gap-24 justify-between">
      <InputField
        id="activeCitationDecisionDate"
        :error-message="
          validationErrors?.find((err) => err.field === 'decisionDate')
            ?.defaultMessage
        "
        label="Entscheidungsdatum"
      >
        <DateInput
          id="activeCitationDecisionDate"
          v-model="activeCitation.decisionDate"
          aria-label="Entscheidungsdatum Aktivzitierung"
        ></DateInput>
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
    <div class="flex gap-24 justify-between">
      <InputField
        id="activeCitationDocumentType"
        :error-message="
          validationErrors?.find((err) => err.field === 'fileNumber')
            ?.defaultMessage
        "
        label="Aktenzeichen"
      >
        <TextInput
          id="activeCitationDocumentType"
          v-model="activeCitation.fileNumber"
          aria-label="Aktenzeichen Aktivzitierung"
          placeholder="Aktenzeichen"
        ></TextInput>
      </InputField>
      <InputField
        id="activeCitationCourt"
        :error-message="
          validationErrors?.find((err) => err.field === 'court')?.defaultMessage
        "
        label="Gericht"
      >
        <ComboboxInput
          id="activeCitationCourt"
          v-model="activeCitation.court"
          aria-label="Gericht Aktivzitierung"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getCourts"
          placeholder="Aktivzitierung Gericht"
        >
        </ComboboxInput>
      </InputField>
    </div>
    <div>
      <TextButton
        aria-label="Nach Entscheidung suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        @click="search(0)"
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
  </div>
</template>
