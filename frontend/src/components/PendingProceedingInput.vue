<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { onMounted, ref, watch } from "vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import InputField from "@/components/input/InputField.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { useScroll } from "@/composables/useScroll"
import RelatedPendingProceeding from "@/domain/pendingProceedingReference"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  modelValue?: RelatedPendingProceeding
  modelValueList?: RelatedPendingProceeding[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: RelatedPendingProceeding]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const { scrollIntoViewportById } = useScroll()
const lastSearchInput = ref(new RelatedPendingProceeding())
const lastSavedModelValue = ref(
  new RelatedPendingProceeding({ ...props.modelValue }),
)
const pendingProceeding = ref(
  new RelatedPendingProceeding({ ...props.modelValue }),
)

const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)

const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

async function search() {
  isLoading.value = true
  const pendingProceedingRef = new RelatedPendingProceeding({
    ...pendingProceeding.value,
  })

  if (
    pendingProceedingRef.documentNumber !==
      lastSearchInput.value.documentNumber ||
    pendingProceedingRef.fileNumber !== lastSearchInput.value.fileNumber
  ) {
    pageNumber.value = 0
  }

  const urlParams = globalThis.location.pathname.split("/")
  const documentNumberToExclude =
    urlParams[urlParams.indexOf("documentunit") + 1]

  const params: Record<string, string> = {
    onlyPendingProceedings: "true",
  }

  if (pageNumber.value !== undefined) {
    params.pg = pageNumber.value.toString()
  }

  if (itemsPerPage.value !== undefined) {
    params.sz = itemsPerPage.value.toString()
  }

  if (documentNumberToExclude !== undefined) {
    params.documentNumber = documentNumberToExclude.toString()
  }

  const response = await documentUnitService.searchByRelatedDocumentation(
    pendingProceedingRef,
    params,
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
  lastSearchInput.value = pendingProceedingRef
  isLoading.value = false
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

async function addRelatedPendingProceedingFromSearch(
  decision: RelatedDocumentation,
) {
  pendingProceeding.value = new RelatedPendingProceeding({
    ...decision,
  })
  emit("update:modelValue", pendingProceeding.value as RelatedPendingProceeding)
  emit("addEntry")
  await scrollIntoViewportById("pendingProceedings")
}

watch(
  () => props.modelValue,
  () => {
    pendingProceeding.value = new RelatedPendingProceeding({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new RelatedPendingProceeding({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  pendingProceeding.value = new RelatedPendingProceeding({
    ...props.modelValue,
  })
})
</script>

<template>
  <div v-ctrl-enter="search" class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="pendingProceedingDocumentNumber"
          v-slot="slotProps"
          label="Dokumentnummer"
        >
          <InputText
            id="pendingProceedingDocumentNumberInput"
            v-model="pendingProceeding.documentNumber"
            aria-label="Dokumentnummer anhängiges Verfahren"
            fluid
            :invalid="slotProps.hasError"
            :readonly="pendingProceeding.isReadOnly"
          />
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          id="pendingProceedingFileNumber"
          v-slot="slotProps"
          label="Aktenzeichen"
        >
          <InputText
            id="pendingProceedingFileNumberInput"
            v-model="pendingProceeding.fileNumber"
            aria-label="Aktenzeichen anhängiges Verfahren"
            fluid
            :invalid="slotProps.hasError"
            :readonly="pendingProceeding.isReadOnly"
          />
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            v-if="!pendingProceeding.isReadOnly"
            aria-label="Nach anhängigen Verfahren suchen"
            label="Suchen"
            size="small"
            @click="search"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>

    <div v-if="isLoading || searchResults" class="bg-blue-200">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="updatePage"
      >
        <SearchResultList
          allow-multiple-links
          :is-loading="isLoading"
          :search-results="searchResults"
          @link-decision="addRelatedPendingProceedingFromSearch"
        />
      </Pagination>
    </div>
  </div>
</template>
