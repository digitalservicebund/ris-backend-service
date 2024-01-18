<script lang="ts" setup>
import { ref } from "vue"
import httpClient from "@/services/httpClient"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const msg = ref("")
const searchInputValue = ref("")

async function handleSearchSubmit() {
  const response = await httpClient.get<string>(
    `search?query=${encodeURIComponent(searchInputValue.value)}`,
  )
  if (response.data) {
    msg.value = response.data
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
          v-model="searchInputValue"
          aria-label="Sucheingabe"
          class="ds-input-medium"
          placeholder="Sucheingabe"
        ></TextInput>
      </InputField>
    </div>
    <div class="py-8">
      <TextButton
        aria-label="Suchen"
        class="self-start"
        label="Suchen"
        size="small"
        @click="handleSearchSubmit"
      />
    </div>
    <p>{{ msg }}</p>
  </header>
</template>
