<script lang="ts" setup>
import { computed, ref } from "vue"
import SearchResultList, {
  SearchResults,
} from "./proceedingDecisions/SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ActiveCitation from "@/domain/activeCitation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{ modelValue?: ActiveCitation }>()
const emit =
  defineEmits<(e: "update:modelValue", value: ActiveCitation) => void>()

const activeCitation = computed({
  get() {
    return props.modelValue as ActiveCitation
  },
  set(value) {
    emit("update:modelValue", value)
  },
})

const activeCitationPredicate = computed({
  get: () =>
    activeCitation?.value?.predicateList
      ? {
          label: activeCitation.value.predicateList,
        }
      : undefined,
  set: (newValue) => {
    let activeCitationRef = new ActiveCitation()
    if (newValue) {
      activeCitationRef = new ActiveCitation({
        ...activeCitation.value,
        predicateList: newValue.label,
      })
    } else delete activeCitationRef.predicateList
    emit("update:modelValue", activeCitationRef)
  },
})

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
</script>

<template>
  <div>
    <InputField
      id="activeCitationPredicate"
      class="border-b-1 border-gray-400 mb-16"
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
      <InputField id="activeCitationDecisionDate" label="Entscheidungsdatum">
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
      <InputField id="activeCitationDocumentType" label="Aktenzeichen">
        <TextInput
          id="activeCitationDocumentType"
          v-model="activeCitation.fileNumber"
          aria-label="Aktenzeichen Aktivzitierung"
          placeholder="Aktenzeichen"
        ></TextInput>
      </InputField>
      <InputField id="activeCitationCourt" label="Gericht">
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
        aria-label="Nach Entscheidungen suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        @click="search(0)"
      />
    </div>
    <div v-if="searchResultsCurrentPage" class="mb-10 mt-20">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="search"
      >
        <SearchResultList :search-results="searchResults" />
      </Pagination>
    </div>
  </div>
</template>
