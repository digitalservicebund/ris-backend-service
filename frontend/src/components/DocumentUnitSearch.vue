<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm, {
  DocumentUnitSearchParameter,
} from "@/components/DocumentUnitSearchEntryForm.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { Query } from "@/composables/useQueryFromRoute"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import service from "@/services/documentUnitService"

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
      />
    </Pagination>
  </div>
</template>
