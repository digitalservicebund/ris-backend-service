<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm, {
  DocumentUnitSearchParameter,
} from "@/components/DocumentUnitSearchEntryForm.vue"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { Query } from "@/composables/useQueryFromRoute"
import { Court } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import ComboboxItemService from "@/services/comboboxItemService"
import service from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const router = useRouter()

const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()

const itemsPerPage = 100
const searchResponseError = ref()
const isLoading = ref(false)
const searchQuery = ref<Query<DocumentUnitSearchParameter>>()
const pageNumber = ref<number>(0)

async function search() {
  isLoading.value = true

  const response = await service.searchByDocumentUnitSearchInput({
    ...(pageNumber.value != undefined
      ? { pg: pageNumber.value.toString() }
      : {}),
    ...(itemsPerPage != undefined ? { sz: itemsPerPage.toString() } : {}),
    ...searchQuery.value,
  })
  if (response.data) {
    documentUnitListEntries.value = response.data.content
    currentPage.value = response.data
  }
  if (response.error) {
    searchResponseError.value = response.error
  }

  if (
    isSearchCompletedWithNoResults.value &&
    !hasFilters.value &&
    searchQuery.value?.courtType
  ) {
    const courtFilter =
      searchQuery.value.courtType +
      (searchQuery.value.courtLocation
        ? ` ${searchQuery.value.courtLocation}`
        : "")

    const courtResponse = (await ComboboxItemService.getCourts(courtFilter))
      .data

    courtFromQuery.value =
      courtResponse &&
      courtResponse.length === 1 &&
      (courtResponse[0].value as Court).label == courtFilter
        ? (courtResponse[0].value as Court)
        : undefined
  } else courtFromQuery.value = undefined
  // --
  isLoading.value = false
}

async function handleDelete(documentUnitListEntry: DocumentUnitListEntry) {
  if (documentUnitListEntries.value) {
    const response = await service.delete(documentUnitListEntry.uuid as string)
    if (response.status === 200) {
      documentUnitListEntries.value = documentUnitListEntries.value.filter(
        (item) => item != documentUnitListEntry,
      )
    } else {
      alert("Fehler beim Löschen der Dokumentationseinheit: " + response.data)
    }
  }
}

async function updatePage(page: number) {
  pageNumber.value = page
  search()
}

async function updateQuery(value: Query<DocumentUnitSearchParameter>) {
  searchQuery.value = value
  pageNumber.value = 0
  search()
}

async function handleReset() {
  documentUnitListEntries.value = undefined
  currentPage.value = undefined
}

const createFromSearchQueryResponseError = ref<ResponseError | undefined>()

async function createFromSearchQuery() {
  isLoading.value = true
  createFromSearchQueryResponseError.value = undefined
  const createResponse = await service.createNew()
  if (createResponse.error) {
    createFromSearchQueryResponseError.value = createResponse.error
    isLoading.value = false
    return
  }

  const docUnit = createResponse.data
  docUnit.coreData.fileNumbers = fileNumberFromQuery.value
    ? [fileNumberFromQuery.value]
    : []
  docUnit.coreData.decisionDate = searchQuery.value?.decisionDate
  docUnit.coreData.court = courtFromQuery.value

  const updateResponse = await service.update(docUnit)
  if (updateResponse.error) {
    createFromSearchQueryResponseError.value = updateResponse.error
    if (docUnit?.uuid) await service.delete(docUnit.uuid)
    isLoading.value = false
    return
  }

  await router.push({
    name: "caselaw-documentUnit-documentNumber-categories",
    params: { documentNumber: createResponse.data.documentNumber },
  })
  isLoading.value = false
}

const fileNumberFromQuery = computed(() => {
  return searchQuery.value?.fileNumber?.trim()
})

const courtFromQuery = ref<Court | undefined>()
const dateFromQuery = computed(() => {
  if (
    searchQuery.value?.decisionDateEnd != undefined ||
    searchQuery.value?.decisionDate == undefined
  )
    return undefined
  return dayjs(searchQuery.value.decisionDate, "YYYY-MM-DD", true).format(
    "DD.MM.YYYY",
  )
})

const isSearchCompletedWithNoResults = computed(
  () =>
    documentUnitListEntries.value != undefined &&
    documentUnitListEntries.value.length === 0,
)
const hasFilters = computed(
  () =>
    searchQuery.value != undefined &&
    (searchQuery.value.documentNumber ||
      searchQuery.value.publicationStatus ||
      searchQuery.value.myDocOfficeOnly),
)
const hasInvalidOrNoQueryData = computed(
  () =>
    searchQuery.value != undefined &&
    !fileNumberFromQuery.value &&
    !courtFromQuery.value &&
    !dateFromQuery.value,
)

const showDefaultLink = computed(() => {
  return (
    !isSearchCompletedWithNoResults.value ||
    hasFilters.value ||
    hasInvalidOrNoQueryData.value
  )
})
</script>

<template>
  <div>
    <DocumentUnitSearchEntryForm
      :is-loading="isLoading"
      @reset-search-results="handleReset"
      @search="updateQuery"
    />
    <Pagination
      :is-loading="isLoading"
      navigation-position="bottom"
      :page="currentPage"
      @update-page="updatePage"
    >
      <DocumentUnitList
        class="grow"
        :document-unit-list-entries="documentUnitListEntries"
        :is-loading="isLoading"
        :search-response-error="searchResponseError"
        @delete-document-unit="handleDelete"
      >
        <template #newlink>
          <TextButton
            v-if="showDefaultLink"
            aria-label="Neue Dokumentationseinheit erstellen"
            button-type="ghost"
            label="Neue Dokumentationseinheit erstellen"
            @click="router.push({ name: 'caselaw-documentUnit-new' })"
          />
          <div v-else class="space-y-16 text-center">
            <div v-if="createFromSearchQueryResponseError" class="mb-24">
              <InfoModal
                :description="createFromSearchQueryResponseError.description"
                :title="createFromSearchQueryResponseError.title"
              />
            </div>
            <template v-else>
              <p>
                Sie können die folgenden Stammdaten übernehmen und eine neue<br />Dokumentationseinheit
                erstellen:
              </p>
              <p class="ds-label-01-bold mb-16 text-center">
                <span :class="{ 'text-gray-800': !fileNumberFromQuery }">{{
                  `${fileNumberFromQuery ?? "Aktenzeichen unbekannt"}, `
                }}</span>
                <span :class="{ 'text-gray-800': !courtFromQuery }">{{
                  `${courtFromQuery?.label ?? "Gericht unbekannt"}, `
                }}</span>
                <span :class="{ 'text-gray-800': !dateFromQuery }">{{
                  dateFromQuery ?? "Datum unbekannt"
                }}</span>
              </p>
              <TextButton
                button-type="tertiary"
                label="Übernehmen und fortfahren"
                size="small"
                @click="createFromSearchQuery"
              />
            </template>
          </div>
        </template>
      </DocumentUnitList>
    </Pagination>
  </div>
</template>
