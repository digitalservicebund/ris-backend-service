<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import values from "@/data/values.json"
import ActiveCitation from "@/domain/activeCitation"
import { CitationType } from "@/domain/citationType"
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
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(30)
const isLoading = ref(false)

const activeCitationType = computed({
  get: () =>
    activeCitation?.value?.citationType
      ? {
          label: activeCitation.value.citationType.label,
          value: activeCitation.value.citationType,
          additionalInformation:
            activeCitation.value.citationType.jurisShortcut,
        }
      : undefined,
  set: (newValue) => {
    if (!newValue)
      validationStore.add("Pflichtfeld nicht befüllt", "citationType")
    if (newValue?.label) {
      activeCitation.value.citationType = { ...newValue }
    } else {
      activeCitation.value.citationType = undefined
    }
  },
})

const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

async function search() {
  isLoading.value = true
  const activeCitationRef = new ActiveCitation({
    ...activeCitation.value,
  })

  if (activeCitationRef.citationType) {
    delete activeCitationRef["citationType"]
  }
  const response = await documentUnitService.searchByRelatedDocumentation(
    pageNumber.value,
    itemsPerPage.value,
    activeCitationRef,
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
  const newActiveCitationType = {
    ...activeCitationType.value?.value,
  } as CitationType
  const decisionWithCitationType = new ActiveCitation({
    ...decision,
    referenceFound: true,
    citationType: newActiveCitationType,
  })
  emit("update:modelValue", decisionWithCitationType)
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
      !activeCitation.value.citationTypeIsSet &&
      !activeCitation.value.isEmpty
    ) {
      validationStore.add("Pflichtfeld nicht befüllt", "citationType")
    } else if (activeCitation.value.citationTypeIsSet) {
      validationStore.remove("citationType")
    }
  },
  { deep: true },
)
</script>

<template>
  <div class="flex flex-col gap-24">
    <div
      v-if="activeCitation.hasForeignSource"
      class="ds-link-01-bold underline"
    >
      {{ activeCitation.renderDecision }}
    </div>
    <InputField
      id="activeCitationPredicate"
      v-slot="slotProps"
      class="border-b-1 border-gray-400"
      label="Art der Zitierung *"
      :validation-error="validationStore.getByField('citationType')"
    >
      <ComboboxInput
        id="activeCitationPredicate"
        v-model="activeCitationType"
        aria-label="Art der Zitierung"
        clear-on-choosing-item
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getCitationTypes"
      ></ComboboxInput>
    </InputField>
    <div v-if="!activeCitation.hasForeignSource" class="flex flex-col gap-24">
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
            class="ds-input-medium"
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
            size="medium"
          ></TextInput>
        </InputField>
        <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
          <ComboboxInput
            id="activeCitationDecisionDocumentType"
            v-model="activeCitation.documentType"
            aria-label="Dokumenttyp der Aktivzitierung"
            :item-service="ComboboxItemService.getDocumentTypes"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
    <div>
      <TextButton
        v-if="!activeCitation.hasForeignSource"
        aria-label="Nach Entscheidung suchen"
        button-type="primary"
        class="mr-24"
        label="Suchen"
        size="small"
        @click="search"
      />
      <TextButton
        aria-label="Aktivzitierung speichern"
        button-type="tertiary"
        :disabled="activeCitation.isEmpty"
        label="Direkt übernehmen"
        size="small"
        @click="addActiveCitation"
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
        @link-decision="addActiveCitationFromSearch"
      />
    </Pagination>
  </div>
</template>
