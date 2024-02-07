<script lang="ts" setup>
import { ref } from "vue"
import httpClient, {
  FailedValidationServerResponse,
} from "@/services/httpClient"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const searchInput = ref("")
const isLoading = ref(false)
const hasError = ref(false)
const message = ref("")
const TIMEOUT = 10000
const searchResults = ref<SearchApiDTO | null>(null)

type SearchApiDTO = {
  totalItems: number
  data: SearchApiDataDTO[]
  next: string
  prev: string
  totalResults: number
  totalPages: number
}

type SearchApiDataDTO = {
  documentNumber: string
  reference: string[]
  court: string
  location: string
  decisionDate: string
  documentType: string
}

async function handleSearchSubmit() {
  isLoading.value = true
  message.value = "Loading ..."
  hasError.value = false
  try {
    const response = await httpClient.get<
      string | FailedValidationServerResponse
    >(`search?query=${encodeURIComponent(searchInput.value)}`, {
      timeout: TIMEOUT,
    })
    if (response.status == 504) {
      message.value = "Request timed out"
    } else if (response.status === 200 && response.data) {
      message.value = response.data as string
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

function handleMockClick() {
  searchResults.value = {
    totalItems: 2,
    data: [
      {
        documentNumber: "ABCD000012345",
        reference: ["II 1234/56 A"],
        court: "AB",
        location: "Berlin",
        decisionDate: "1990-01-02",
        documentType: "Urteil",
      },
      {
        documentNumber: "EFGH000067890",
        reference: ["II 7890/12 B"],
        court: "CD",
        location: "Hamburg",
        decisionDate: "1999-03-04",
        documentType: "Urteil",
      },
    ],
    next: "",
    prev: "",
    totalResults: 2,
    totalPages: 1,
  }
}
</script>

<template>
  <header class="bg-white px-16 py-16">
    <h1 class="ds-heading-02-reg">Suche</h1>
    <div class="mt-32">
      <InputField id="searchInput" label="Suche" visually-hide-label>
        <TextInput
          id="searchInput"
          v-model="searchInput"
          aria-label="Sucheingabe"
          class="ds-input-medium"
          :disabled="isLoading"
          placeholder="Sucheingabe"
        ></TextInput>
      </InputField>
    </div>
    <div class="py-8">
      <TextButton
        aria-label="Suchen"
        class="self-start"
        :disabled="isLoading"
        label="Suchen"
        size="small"
        @click="handleSearchSubmit"
      />
      <TextButton
        aria-label="Mock"
        class="self-start"
        :disabled="isLoading"
        label="Mock"
        size="small"
        @click="handleMockClick"
      />
    </div>
    <p :class="{ 'text-red-700': hasError }">{{ message }}</p>
    <div
      v-if="searchResults && searchResults.data.length > 0"
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
      </div>
      <div
        v-for="(item, index) in searchResults.data"
        :key="index"
        class="ds-label-01-reg table-row hover:bg-gray-100"
      >
        <div
          class="min-h-56 table-cell border-b-1 border-blue-300 px-16 py-12 align-middle"
        >
          {{ item.documentNumber }}
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
      </div>
    </div>
  </header>
</template>
