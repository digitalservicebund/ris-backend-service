<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import service from "@/services/documentUnitService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()
const searchInput = ref<DocumentUnitListEntry | undefined>(undefined)

const itemsPerPage = 30

async function search(page = 0, listEntry?: DocumentUnitListEntry) {
  if (listEntry) {
    searchInput.value = listEntry
  }
  console.log(searchInput.value)
  const response = await service.searchByDocumentUnitListEntry(
    page,
    itemsPerPage,
    searchInput.value,
  )
  console.log(response)
  if (response.data) {
    documentUnitListEntries.value = response.data.content
    currentPage.value = response.data
  } else {
    console.error("could not load list entries")
  }
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

async function handleSearch(listEntry: DocumentUnitListEntry) {
  await search(0, listEntry)
}
</script>

<template>
  <div>
    <DocumentUnitSearchEntryForm @search="handleSearch" />
    <Pagination
      v-if="currentPage"
      navigation-position="bottom"
      :page="currentPage"
      @update-page="search"
    >
      <DocumentUnitList
        v-if="documentUnitListEntries"
        class="grow"
        :document-unit-list-entries="documentUnitListEntries"
        @delete-document-unit="handleDelete"
      />
    </Pagination>
  </div>
</template>
