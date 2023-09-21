<script lang="ts" setup>
import { onMounted, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import DocumentUnitSearchInput from "@/domain/documentUnitSearchInput"
import service from "@/services/documentUnitService"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const documentUnitListEntries = ref<DocumentUnitListEntry[]>()
const currentPage = ref<Page<DocumentUnitListEntry>>()

const itemsPerPage = 30
const searchResponseError = ref()
const isLoading = ref(false)

const route = useRoute()
const router = useRouter()

const searchQuery = ref<{ [key: string]: string }>()

async function search(page = 0, searchInput?: DocumentUnitSearchInput) {
  isLoading.value = true

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const requestParams: { [key: string]: any } = {
    ...(searchInput?.documentNumberOrFileNumber
      ? {
          documentNumberOrFileNumber: searchInput?.documentNumberOrFileNumber,
        }
      : {}),
    ...(searchInput?.courtType ? { courtType: searchInput?.courtType } : {}),
    ...(searchInput?.courtLocation
      ? { courtLocation: searchInput?.courtLocation }
      : {}),
    ...(searchInput?.decisionDate
      ? { decisionDate: searchInput?.decisionDate }
      : {}),
    ...(searchInput?.decisionDateEnd
      ? { decisionDateEnd: searchInput?.decisionDateEnd }
      : {}),
    ...(searchInput?.status?.publicationStatus
      ? { publicationStatus: searchInput?.status.publicationStatus }
      : {}),
    ...(searchInput?.status?.withError
      ? { withError: searchInput?.status.withError }
      : {}),
    ...(searchInput?.myDocOfficeOnly
      ? { myDocOfficeOnly: searchInput?.myDocOfficeOnly }
      : {}),
  }

  searchQuery.value = requestParams

  const response = await service.searchByDocumentUnitSearchInput({
    ...(page != undefined ? { pg: page.toString() } : {}),
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

async function handleSearch(searchInput: DocumentUnitSearchInput) {
  await search(0, searchInput)
}

async function handleReset() {
  documentUnitListEntries.value = undefined
  currentPage.value = undefined
}

watch(
  searchQuery,
  () => {
    router.push(searchQuery.value ? { query: searchQuery.value } : {})
  },
  { deep: true },
)

onMounted(async () => {
  if (route.query) {
    await search(0, route.query)
    searchQuery.value = route.query as { [key: string]: string }
  }
})
</script>

<template>
  <div>
    <DocumentUnitSearchEntryForm
      :is-loading="isLoading"
      :model-value="searchQuery"
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
