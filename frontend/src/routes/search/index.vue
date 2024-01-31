<script lang="ts" setup>
import { ref } from "vue"
import httpClient from "@/services/httpClient"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const searchInput = ref("")
const isLoading = ref(false)
const hasError = ref(false)
const message = ref("")

async function handleSearchSubmit() {
  isLoading.value = true
  message.value = "Loading ..."
  hasError.value = false
  try {
    const response = await httpClient.get<string>(
      `search?query=${encodeURIComponent(searchInput.value)}`,
    )
    if (response.data) {
      message.value = response.data
    }
    if (response.status !== 200) {
      hasError.value = true
    }
  } catch (error) {
    hasError.value = true
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
