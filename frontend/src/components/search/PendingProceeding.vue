<script lang="ts" setup>
import { computed, ref } from "vue"
import ResultList from "./shared/ResultList.vue"
import SearchForm from "./shared/SearchForm.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import { Page } from "@/components/Pagination.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { Query } from "@/composables/useQueryFromRoute"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"

const isLoading = ref(false)
const pageNumber = ref<number>(0)
const itemsPerPage = 100
const searchQuery = ref<Query<DocumentationUnitSearchParameter>>()
const currentPage = ref<Page<DocumentUnitListEntry>>()
const serviceError = ref<ResponseError>()
const isInternalUser = useInternalUser()

const emptyStateLabel = computed(() => {
  if (!currentPage.value?.content) {
    if (isInternalUser.value) {
      return "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit."
    } else {
      return "Starten Sie die Suche."
    }
  } else if (
    currentPage.value?.content &&
    currentPage.value.content.length === 0
  ) {
    return errorMessages.SEARCH_RESULTS_NOT_FOUND.title
  }
  return undefined
})

/**
 * Searches all documentation units by given input and updates the local
 * documentunit list entries, the currentPage for pagination and catches errors
 * if they happen. When no results found, but a court was given, the court input
 * is validated against the court table, in order to be able to create a new
 * documentation unit from search the given search input, if the user wants to.
 */
async function search() {
  isLoading.value = true
  if (currentPage.value) currentPage.value.content = []

  const response = await service.searchByDocumentUnitSearchInput({
    ...(pageNumber.value != undefined
      ? { pg: pageNumber.value.toString() }
      : {}),
    ...(itemsPerPage != undefined ? { sz: itemsPerPage.toString() } : {}),
    ...searchQuery.value,
  })
  if (response.data) {
    currentPage.value = response.data
    serviceError.value = undefined
  }
  if (response.error) {
    serviceError.value = response.error
  }
  isLoading.value = false
}

/**
 * Deletes a documentation unit
 * @param {DocumentUnitListEntry} documentUnitListEntry - The entry in the list to be removed
 */
async function handleDelete(documentUnitListEntry: DocumentUnitListEntry) {
  if (currentPage.value?.content) {
    const response = await service.delete(documentUnitListEntry.uuid as string)
    if (response.status === 200) {
      if (currentPage.value.content.length === 1) {
        await search()
        return
      }
      const newEntries = currentPage.value.content.filter(
        (item: DocumentUnitListEntry) => item != documentUnitListEntry,
      )
      currentPage.value.content = newEntries
      currentPage.value.numberOfElements = newEntries.length
      currentPage.value.empty = newEntries.length == 0
    } else {
      alert("Fehler beim Löschen der Dokumentationseinheit: " + response.data)
    }
  }
}

/**
 * When using the navigation a new page number is set, the search is triggered,
 * with the given page number.
 * @param {number} page - The page to be updated
 */
async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

/**
 * The search form emits an event, when clicking the search button and is
 * triggering the search with the updated query here.
 * It will always reset the pagination to the first page.
 * @param {Query<DocumentationUnitSearchParameter>} value - The page to be updated
 */
async function updateQuery(value: Query<DocumentationUnitSearchParameter>) {
  searchQuery.value = value
  pageNumber.value = 0
  await search()
}

/**
 * The search form emits an event, when resetting the search,
 * which empties the document unit list and resets the pagination.
 */
async function handleReset() {
  currentPage.value = undefined
}
</script>

<template>
  <div class="flex flex-col" data-testId="eu-inbox">
    <SearchForm
      :is-loading="isLoading"
      @reset-search-results="handleReset"
      @search="updateQuery"
    />
    <InfoModal
      v-if="serviceError"
      class="my-16"
      data-testid="service-error"
      :description="serviceError.description"
      :status="InfoStatus.ERROR"
      :title="serviceError.title"
    />
    <ResultList
      :empty-text="emptyStateLabel"
      :loading="isLoading"
      :page-entries="currentPage"
      @delete-documentation-unit="handleDelete"
      @update-page="updatePage"
    />
  </div>
</template>
