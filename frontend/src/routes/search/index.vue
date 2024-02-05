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
</script>

<template>
  <header class="container mx-auto flex flex-col gap-16 px-16 py-16">
    <div
      aria-label="Infomodal"
      class="flex w-full gap-[0.625rem] border-l-[0.125rem] border-l-yellow-800 bg-yellow-200 px-[1.25rem] py-[1.125rem]"
    >
      <svg
        viewBox="0 0 24 24"
        width="1.3333em"
        height="1.3333em"
        class="text-yellow-800"
        aria-label="Es ist ein Fehler aufgetreten icon"
      >
        <path
          fill="currentColor"
          d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8s8 3.58 8 8s-3.58 8-8 8z"
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
            :to="`/search/help`"
            target="_blank"
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
          :to="`/search/help`"
          target="_blank"
        >
          Hilfe
        </router-link>
      </div>

      <p :class="{ 'text-red-700': hasError }">{{ message }}</p>
    </div>
  </header>
</template>
