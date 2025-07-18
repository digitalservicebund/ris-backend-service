<script lang="ts" setup>
import dayjs from "dayjs"
import Button from "primevue/button"
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import ResultList from "./shared/ResultList.vue"
import SearchForm from "./shared/SearchForm.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import { Page } from "@/components/Pagination.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { Query } from "@/composables/useQueryFromRoute"
import { Court } from "@/domain/court"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import errorMessages from "@/i18n/errors.json"
import ComboboxItemService from "@/services/comboboxItemService"
import service from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import { DocumentationUnitCreationParameters } from "@/types/documentationUnitCreationParameters"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"

const router = useRouter()
const currentPage = ref<Page<DocumentUnitListEntry>>()

const pageNumber = ref<number>(0)
const itemsPerPage = 100
const serviceError = ref<ResponseError | undefined>(undefined)
const isLoading = ref(false)
const searchQuery = ref<Query<DocumentationUnitSearchParameter>>()
const isInternalUser = useInternalUser()

const emptyStateMessage = computed(() => {
  if (!currentPage.value?.content) {
    return isInternalUser.value
      ? "Starten Sie die Suche oder erstellen Sie ein neues Anhängiges Verfahren."
      : "Starten Sie die Suche."
  }
  if (currentPage.value.content.length === 0) {
    return errorMessages.SEARCH_RESULTS_NOT_FOUND.title
  }
  return undefined
})

const fileNumberFromQuery = computed(() =>
  searchQuery.value?.fileNumber?.trim(),
)

const dateFromQuery = computed(() => {
  if (
    (searchQuery.value?.decisionDateEnd != undefined &&
      searchQuery.value?.decisionDateEnd != searchQuery.value?.decisionDate) ||
    searchQuery.value?.decisionDate == undefined
  ) {
    return undefined
  }
  return searchQuery.value?.decisionDate
})

const isSearchCompletedWithNoResults = computed(
  () => currentPage.value?.content && currentPage.value.content.length === 0,
)

const courtFilter = ref("")
const { data: courts, execute: fetchCourts } =
  ComboboxItemService.getCourts(courtFilter)
const courtFromQuery = ref<Court | undefined>()

const hasRequiredCreateParams = computed(() => {
  const query = searchQuery.value
  return (
    !!query &&
    (query.courtType ||
      query.courtLocation ||
      query.fileNumber ||
      query.decisionDate)
  )
})

const showCreateFromParamsButton = computed(
  () =>
    isInternalUser.value &&
    isSearchCompletedWithNoResults.value &&
    hasRequiredCreateParams.value,
)

async function validateAndSetCourtFromQuery() {
  if (isSearchCompletedWithNoResults.value && searchQuery.value?.courtType) {
    let courtFilterValue = searchQuery.value.courtType
    if (searchQuery.value.courtLocation) {
      courtFilterValue += ` ${searchQuery.value.courtLocation}`
    }
    courtFilter.value = courtFilterValue
    await fetchCourts()

    const matches = courts.value?.filter(
      (item) => item.label === courtFilter.value,
    )
    courtFromQuery.value =
      matches?.length === 1 ? (matches[0].value as Court) : undefined
  } else {
    courtFromQuery.value = undefined
  }
}

async function search() {
  isLoading.value = true
  currentPage.value = undefined

  const params = {
    pg: pageNumber.value.toString(),
    sz: itemsPerPage.toString(),
    ...searchQuery.value,
    kind: "PENDING_PROCEEDING",
  }

  const response = await service.searchByDocumentUnitSearchInput(params)

  if (response.data) {
    currentPage.value = response.data
    serviceError.value = undefined
  }
  if (response.error) {
    serviceError.value = response.error
  }

  await validateAndSetCourtFromQuery()
  isLoading.value = false
}

async function handleDelete(documentUnit: DocumentUnitListEntry) {
  if (!documentUnit.uuid) return

  const response = await service.delete(documentUnit.uuid)
  if (response.status === 200) {
    if (currentPage.value?.content?.length === 1) {
      await search() // Re-fetch if the last item on the page was deleted
    } else if (currentPage.value?.content) {
      currentPage.value.content = currentPage.value.content.filter(
        (item) => item.uuid !== documentUnit.uuid,
      )
      currentPage.value.numberOfElements = currentPage.value.content.length
      currentPage.value.empty = currentPage.value.content.length === 0
    }
  } else {
    serviceError.value = response.error || {
      title: "Error",
      description: "Fehler beim Löschen des Anhängigen Verfahrens.",
    }
  }
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

async function updateQuery(value: Query<DocumentationUnitSearchParameter>) {
  searchQuery.value = value
  pageNumber.value = 0
  await search()
}

function handleReset() {
  currentPage.value = undefined
  serviceError.value = undefined
  courtFromQuery.value = undefined
}

const createNew = async () => {
  await router.push({ name: "caselaw-pending-proceeding-new" })
}

async function createNewFromSearch() {
  if (!searchQuery.value) {
    return
  }

  const requestBodyParameters: DocumentationUnitCreationParameters = {
    fileNumber: searchQuery.value.fileNumber,
    decisionDate: searchQuery.value.decisionDate,
    court: courtFromQuery.value,
  }

  const createResponse = await service.createNew(requestBodyParameters, {
    kind: Kind.PENDING_PROCEEDING,
  })

  if (createResponse.error) {
    serviceError.value = createResponse.error
    return
  }

  await router.push({
    name: "caselaw-pending-proceeding-documentNumber-categories",
    params: { documentNumber: createResponse.data.documentNumber },
  })
}
</script>

<template>
  <div class="flex flex-col gap-24" data-testId="pending-proceeding-search">
    <SearchForm
      :is-loading="isLoading"
      :kind="Kind.PENDING_PROCEEDING"
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
      :kind="Kind.PENDING_PROCEEDING"
      :loading="isLoading"
      :page-entries="currentPage"
      @delete-documentation-unit="handleDelete"
      @update-page="updatePage"
    >
      <template #empty-state-content>
        <div class="flex flex-col gap-16 text-black">
          <p>{{ emptyStateMessage }}</p>

          <div v-if="showCreateFromParamsButton">
            <p>
              Sie können die folgenden Stammdaten übernehmen und ein neues<br />Anhängiges
              Verfahren erstellen:
            </p>
            <p class="ris-label1-bold mb-16 text-center">
              <span :class="{ 'text-gray-800': !fileNumberFromQuery }"
                >{{ fileNumberFromQuery ?? "Aktenzeichen unbekannt" }},
              </span>
              <span :class="{ 'text-gray-800': !courtFromQuery }"
                >{{ courtFromQuery?.label ?? "Gericht unbekannt" }},
              </span>
              <span :class="{ 'text-gray-800': !dateFromQuery }">
                {{
                  dayjs(dateFromQuery).format("DD.MM.YYYY") ?? "Datum unbekannt"
                }}
              </span>
            </p>
            <Button
              class="justify-self-center"
              label="Übernehmen und fortfahren"
              severity="secondary"
              size="small"
              @click="createNewFromSearch"
            />
          </div>
          <div v-else-if="isInternalUser">
            <Button
              aria-label="Neues Anhängiges Verfahren erstellen"
              label="Neues Anhängiges Verfahren erstellen"
              text
              @click="createNew"
            />
          </div>
        </div>
      </template>
    </ResultList>
  </div>
</template>
