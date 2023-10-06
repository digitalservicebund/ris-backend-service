<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm, {
  DocumentUnitSearchParameter,
} from "@/components/DocumentUnitSearchEntryForm.vue"
import { Query } from "@/composables/useQueryFromRoute"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import service from "@/services/documentUnitService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()

const itemsPerPage = 30
const searchResponseError = ref()
const isLoading = ref(false)

async function search(page = 0, query?: Query<string>) {
  isLoading.value = true

  const response = await service.searchByDocumentUnitSearchInput({
    ...(page != undefined ? { pg: page.toString() } : {}),
    ...(itemsPerPage != undefined ? { sz: itemsPerPage.toString() } : {}),
    ...query,
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

async function handleSearch(value: Query<DocumentUnitSearchParameter>) {
  await search(0, value)
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
      @search="handleSearch"
    />
    <Pagination
      :is-loading="isLoading"
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
