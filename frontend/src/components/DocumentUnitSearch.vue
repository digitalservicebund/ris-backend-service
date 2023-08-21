<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import DocumentUnitSearchInput from "@/domain/documentUnitSearchInput"
import service from "@/services/documentUnitService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()
const searchInput = ref<DocumentUnitListEntry | undefined>(undefined)

const itemsPerPage = 30
const searchResponseError = ref()
const isLoading = ref(false)

async function search(page = 0, listEntry?: DocumentUnitSearchInput) {
  isLoading.value = true
  if (listEntry) {
    searchInput.value = listEntry
  }
  const response = await service.searchByDocumentUnitSearchInput(
    page,
    itemsPerPage,
    searchInput.value,
  )
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

async function handleSearch(listEntry: DocumentUnitSearchInput) {
  await search(0, listEntry)
}

async function handleReset() {
  documentUnitListEntries.value = []
}
</script>

<template>
  <div>
    <DocumentUnitSearchEntryForm
      @reset-search-results="handleReset"
      @search="handleSearch"
    />
    <Pagination
      navigation-position="bottom"
      :page="currentPage"
      @update-page="search"
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
