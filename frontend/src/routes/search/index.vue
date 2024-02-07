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
    </div>
    <p :class="{ 'text-red-700': hasError }">{{ message }}</p>
  </header>
</template>
