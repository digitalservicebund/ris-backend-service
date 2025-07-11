<script setup lang="ts">
import dayjs from "dayjs"
import Button from "primevue/button"
import { onMounted, ref } from "vue"
import CopyableLabel from "@/components/CopyableLabel.vue"
import { ApiKey } from "@/domain/apiKey"
import authService from "@/services/authService"

const apiKey = ref<ApiKey | null>(null)
const isLoading = ref(true)

async function generateApiKey() {
  const response = await authService.generateImportApiKey()
  if (response.data) apiKey.value = response.data
}

async function invalidateApiKey() {
  if (apiKey.value?.apiKey) {
    const response = await authService.invalidateImportApiKey(
      apiKey.value?.apiKey,
    )
    if (response.data) apiKey.value = response.data
  }
}

onMounted(async () => {
  const response = await authService.getImportApiKey()
  if (response.data) apiKey.value = response.data
  isLoading.value = false // Set loading to false after fetch
})
</script>

<template>
  <div class="flex-col gap-16">
    <span class="ris-label2-bold">API Key</span>
    <div v-if="isLoading" class="ris-body1-regular mt-24">
      Loading API Key...
    </div>
    <div v-else-if="apiKey">
      <div class="ris-body1-regular mt-24">
        <CopyableLabel
          v-if="apiKey.valid"
          name="API Key"
          :text="apiKey.apiKey"
        />
        <div
          v-if="apiKey.valid"
          class="ris-label2-regular-italic mt-24 text-gray-900"
        >
          gültig bis:
          {{ dayjs(apiKey.validUntil).format("DD.MM.YYYY HH:mm:ss") }}
        </div>
        <div v-else class="ris-label2-bold mt-24 text-red-900">
          API-Key ist abgelaufen!
        </div>
      </div>
      <div v-if="apiKey.valid">
        <Button label="Sperren" @click="invalidateApiKey"></Button>
      </div>
      <div v-else>
        <Button
          label="Neuen API-Schlüssel erstellen"
          @click="generateApiKey"
        ></Button>
      </div>
    </div>
    <div v-else>
      <Button
        label="Neuen API-Schlüssel erstellen"
        @click="generateApiKey"
      ></Button>
    </div>
  </div>
</template>
