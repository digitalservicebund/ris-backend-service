<script lang="ts" setup>
import { ref } from "vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import httpClient, {
  FailedValidationServerResponse,
} from "@/services/httpClient"

const searchInput = ref("")
const isLoading = ref(false)
const hasError = ref(false)
const message = ref("")
const TIMEOUT = 10000
const searchResults = ref<SearchApiDataDTO[] | undefined>()
const currentPage = ref<Page<SearchApiDataDTO> | undefined>()
const itemsPerPage = 100
const pageNumber = ref<number>(0)

type DocumentType = "CASELAW" | "LEGISLATION"

type SearchApiDataDTO = {
  documentNumber: string
  reference: string[]
  court: string
  location: string
  decisionDate: string
  documentSubType: string
  documentType: DocumentType
}

async function handleSearchSubmit() {
  if (!searchInput.value) {
    message.value = "Bitte geben Sie eine Suchanfrage ein"
    hasError.value = true
    return
  }
  isLoading.value = true
  message.value = "Loading ..."
  hasError.value = false
  currentPage.value = undefined
  searchResults.value = undefined
  pageNumber.value = 0

  await search()
}

async function search() {
  try {
    const response = await httpClient.get<
      Page<SearchApiDataDTO> | FailedValidationServerResponse
    >(
      `search?query=${encodeURIComponent(searchInput.value)}&sz=${itemsPerPage}&pg=${pageNumber.value}`,
      {
        timeout: TIMEOUT,
      },
    )
    if (response.status == 504) {
      message.value = "Zeitüberschreitung der Anfrage"
    } else if (response.status === 200 && response.data) {
      message.value = ""
      const page = response.data as Page<SearchApiDataDTO>
      currentPage.value = page
      searchResults.value = page.content
    } else {
      hasError.value = true
      const errorResponse = response.data as FailedValidationServerResponse
      message.value = errorResponse.errors.map((e) => e.message).join(", ")
    }
  } catch (error) {
    hasError.value = true
    message.value = error as string
  } finally {
    isLoading.value = false
  }
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}
</script>

<template>
  <header class="container mx-auto flex flex-col gap-16 px-16 py-16">
    <div
      aria-label="Infomodal"
      class="flex w-full gap-[0.625rem] border-l-[0.125rem] border-l-yellow-800 bg-yellow-200 px-[1.25rem] py-[1.125rem]"
    >
      <svg
        aria-label="Es ist ein Fehler aufgetreten icon"
        class="text-yellow-800"
        height="1.3333em"
        viewBox="0 0 24 24"
        width="1.3333em"
      >
        <path
          d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8s8 3.58 8 8s-3.58 8-8 8z"
          fill="currentColor"
        />
      </svg>
      <div class="flex flex-col">
        <span class="ds-label-02-bold">
          Die Suche ist aktuell ein Prototyp
        </span>
        <span class="ds-body-01-reg">
          Die Funktionalität der Suche befindet sich gerade in der Entwicklung
          und ist zum Testen für
          <strong>veröffentlichte</strong> Dokumentationseinheiten verfügbar.
          <router-link
            class="underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
            target="_blank"
            to="/search/help"
          >
            Lesen Sie hier mehr über die aktuelle Funktionalität.
          </router-link>
        </span>
      </div>
    </div>

    <h1 class="ds-heading-02-reg">Suche</h1>

    <div class="flex flex-col gap-4">
      <div>
        <InputField id="searchInput" label="Suche" visually-hide-label>
          <TextInput
            id="searchInput"
            v-model="searchInput"
            aria-label="Suchanfrage"
            class="ds-input-medium"
            :disabled="isLoading"
            placeholder="Suchanfrage"
          />
        </InputField>
      </div>

      <div class="flex flex-row items-center gap-8">
        <TextButton
          aria-label="Suchen"
          class="self-start"
          :disabled="isLoading"
          label="Suchen"
          size="small"
          @click="handleSearchSubmit"
        />
        <router-link
          class="block underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          target="_blank"
          to="/search/help"
        >
          Hilfe
        </router-link>
      </div>
      <p :class="{ 'text-red-700': hasError }">{{ message }}</p>
    </div>
    <Pagination
      :is-loading="isLoading"
      navigation-position="bottom"
      :page="currentPage"
      @update-page="updatePage"
    >
      <div
        v-if="searchResults && searchResults.length > 0"
        class="relative mt-8 table w-full border-separate"
      >
        <div
          class="ds-label-02-bold sticky top-0 table-row bg-white text-gray-900"
        >
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Dokumentnummer
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Referenzen
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Gericht
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Ort
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Entscheidungsdatum
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            Dokumenttyp
          </div>
          <div
            class="table-cell border-b-2 border-solid border-blue-300 px-16 py-12"
          >
            DokumentSubtyp
          </div>
        </div>
        <div
          v-for="(item, index) in searchResults"
          :key="index"
          class="ds-label-01-reg table-row hover:bg-gray-100"
        >
          <div
            class="table-cell min-h-56 border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            <router-link
              class="underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
              :to="{
                name: 'caselaw-documentUnit-documentNumber-categories',
                params: { documentNumber: item.documentNumber },
              }"
            >
              {{ item.documentNumber }}
            </router-link>
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.reference.join(", ") }}
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.court }}
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.location }}
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.decisionDate }}
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.documentType }}
          </div>
          <div
            class="table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
          >
            {{ item.documentSubType }}
          </div>
        </div>
      </div>
    </Pagination>
  </header>
</template>
