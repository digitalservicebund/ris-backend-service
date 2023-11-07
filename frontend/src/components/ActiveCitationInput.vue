<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
import ActiveCitation from "@/domain/activeCitation"
import { CitationStyle } from "@/domain/citationStyle"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{
  modelValue?: ActiveCitation
  modelValueList?: ActiveCitation[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: ActiveCitation]
  addEntry: [void]
}>()

const activeCitation = ref(new ActiveCitation({ ...props.modelValue }))

const validationStore =
  useValidationStore<(typeof ActiveCitation.fields)[number]>()

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
    if (newValue?.label) {
      activeCitation.value.citationStyle = { ...newValue }
    } else {
      activeCitation.value.citationStyle = undefined
    }
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
  const response = await documentUnitService.searchByRelatedDocumentation(
    page,
    30,
    activeCitationRef,
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new ActiveCitation({ ...decision }),
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new ActiveCitation({ ...searchResult }),
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

  activeCitation.value.missingRequiredFields.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function addActiveCitation() {
  validateRequiredInput()
  emit("update:modelValue", activeCitation.value as ActiveCitation)
  emit("addEntry")
}

async function addActiveCitationFromSearch(decision: RelatedDocumentation) {
  const newActiveCitationStyle = {
    ...activeCitationStyle.value?.value,
  } as CitationStyle
  const decisionWithCitationStyle = new ActiveCitation({
    ...decision,
    citationStyle: newActiveCitationStyle,
  })
  emit("update:modelValue", decisionWithCitationStyle)
  emit("addEntry")
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("activeCitations")
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
  if (props.modelValue?.isEmpty !== undefined) {
    validateRequiredInput()
  }
  activeCitation.value = new ActiveCitation({ ...props.modelValue })
})

watch(
  activeCitation,
  () => {
    if (
      !activeCitation.value.citationStyleIsSet &&
      !activeCitation.value.isEmpty
    ) {
      validationStore.add("Pflichtfeld nicht befüllt", "citationStyle")
    } else if (activeCitation.value.citationStyleIsSet) {
      validationStore.remove("citationStyle")
    }
  },
  { deep: true },
)
</script>

<template>
  <div>
    <!-- Todo implement linked logic  -->
    <!-- <div
      v-if="activeCitation.hasForeignSource"
      class="ds-link-01-bold mb-24 underline"
    >
      {{ activeCitation.renderDecision }}
    </div> -->
    <InputField
      id="activeCitationPredicate"
      v-slot="slotProps"
      class="mb-16 border-b-1 border-gray-400"
      label="Art der Zitierung *"
      :validation-error="validationStore.getByField('citationStyle')"
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
    <!-- Todo implement linked logic  -->
    <!-- <div v-if="!activeCitation.hasForeignSource"> -->
    <div>
      <div class="flex justify-between gap-24">
        <InputField
          id="activeCitationCourt"
          v-slot="slotProps"
          label="Gericht *"
          :validation-error="validationStore.getByField('court')"
        >
          <ComboboxInput
            id="activeCitationCourt"
            v-model="activeCitation.court"
            aria-label="Gericht der Aktivzitierung"
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
          :validation-error="validationStore.getByField('decisionDate')"
        >
          <DateInput
            id="activeCitationDecisionDate"
            v-model="activeCitation.decisionDate"
            aria-label="Entscheidungsdatum der Aktivzitierung"
            :has-error="slotProps.hasError"
            @update:validation-error="slotProps.updateValidationError"
          ></DateInput>
        </InputField>
      </div>
      <div class="flex justify-between gap-24">
        <InputField
          id="activeCitationFileNumber"
          v-slot="slotProps"
          label="Aktenzeichen *"
          :validation-error="validationStore.getByField('fileNumber')"
        >
          <TextInput
            id="activeCitationDocumentType"
            v-model="activeCitation.fileNumber"
            aria-label="Aktenzeichen der Aktivzitierung"
            :has-error="slotProps.hasError"
            placeholder="Aktenzeichen"
          ></TextInput>
        </InputField>
        <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
          <ComboboxInput
            id="activeCitationDecisionDocumentType"
            v-model="activeCitation.documentType"
            aria-label="Dokumenttyp der Aktivzitierung"
            :item-service="ComboboxItemService.getDocumentTypes"
            placeholder="Bitte auswählen"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <div>
      <!-- Todo implement linked logic  -->
      <!-- v-if="!activeCitation.hasForeignSource" -->
      <TextButton
        aria-label="Nach Entscheidung suchen"
        button-type="secondary"
        class="mr-28"
        label="Suchen"
        size="small"
        @click="handleSearch"
      />
      <TextButton
        aria-label="Aktivzitierung speichern"
        class="mr-28"
        :disabled="activeCitation.isEmpty"
        label="Übernehmen"
        size="small"
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
