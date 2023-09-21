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
const searchInput = ref<DocumentUnitSearchInput | undefined>(undefined)

const itemsPerPage = 30
const searchResponseError = ref()
const isLoading = ref(false)

const route = useRoute()
const router = useRouter()

const queries = ref<{ [key: string]: string }>()

async function search(page = 0, listEntry?: DocumentUnitSearchInput) {
  isLoading.value = true
  if (listEntry) {
    searchInput.value = listEntry
  }

  const requestParams: { pg?: string; sz?: string; q?: string } = {
    ...(page != undefined ? { pg: page.toString() } : {}),
    ...(itemsPerPage != undefined ? { sz: itemsPerPage.toString() } : {}),
    ...(searchInput.value?.documentNumberOrFileNumber
      ? {
          documentNumberOrFileNumber:
            searchInput.value?.documentNumberOrFileNumber,
        }
      : {}),
    ...(searchInput.value?.courtType
      ? { courtType: searchInput.value?.courtType }
      : {}),
    ...(searchInput.value?.courtLocation
      ? { courtLocation: searchInput.value?.courtLocation }
      : {}),
    ...(searchInput.value?.decisionDate
      ? { decisionDate: searchInput.value?.decisionDate }
      : {}),
    ...(searchInput.value?.decisionDateEnd
      ? { decisionDateEnd: searchInput.value?.decisionDateEnd }
      : {}),
    ...(searchInput.value?.status?.publicationStatus
      ? { publicationStatus: searchInput.value?.status.publicationStatus }
      : {}),
    ...(searchInput.value?.status?.withError
      ? { withError: searchInput.value?.status.withError }
      : {}),
    ...(searchInput.value?.myDocOfficeOnly
      ? { myDocOfficeOnly: searchInput.value?.myDocOfficeOnly }
      : {}),
  }

  queries.value = requestParams

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
  documentUnitListEntries.value = undefined
  currentPage.value = undefined
}

watch(
  queries,
  () => {
    router.push(queries.value ? { query: queries.value } : {})
  },
  { deep: true },
)

onMounted(async () => {
  if (route.query) {
    console.log
    searchInput.value = route.query
    await search(0, route.query)
  }
})
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
