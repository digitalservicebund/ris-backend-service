<script lang="ts" setup>
import dayjs from "dayjs"
import { ref, onMounted } from "vue"
import TextButton from "@/components/input/TextButton.vue"
import { ApiKey } from "@/domain/apiKey"
import authService from "@/services/authService"

const apiKey = ref<ApiKey>()
const copyText = ref("Kopieren")
const copied = ref(false)
const copyButtonEnabled = ref(true)

async function generateApiKey() {
  const response = await authService.generateImportApiKey()

  if (response.data) apiKey.value = response.data

  copyText.value = "Kopieren"
  copied.value = false
  copyButtonEnabled.value = true
}

async function invalidateApiKey() {
  if (apiKey.value?.apiKey) {
    const response = await authService.invalidateImportApiKey(
      apiKey.value?.apiKey,
    )

    if (response.data) apiKey.value = response.data

    copyText.value = "Kopieren"
    copied.value = false
    copyButtonEnabled.value = false
  }
}

function copyKey() {
  if (apiKey.value) {
    navigator.clipboard.writeText(apiKey.value.apiKey)

    copyText.value = "Kopiert!"
    copied.value = true
    copyButtonEnabled.value = false
  }
}

onMounted(async () => {
  const response = await authService.getImportApiKey()
  if (response.data) apiKey.value = response.data

  copyText.value = "Kopieren"
  copied.value = false

  if (apiKey.value?.valid) {
    copyButtonEnabled.value = true
  } else {
    copyButtonEnabled.value = false
  }
})
</script>

<template>
  <h3 class="ds-heading-03-bold ml-64 mt-48">Einstellungen</h3>
  <div class="mt-56 min-h-screen bg-gray-100 pl-64 pt-56" v-bind="$attrs">
    <span class="ds-label-02-bold">API Key</span>
    <div v-if="apiKey">
      <div class="ds-body-01-reg mt-24">
        {{ apiKey.apiKey }}
        <TextButton
          class="ds-button-tertiary ml-20"
          :class="
            copied
              ? 'disabled:bg-blue-200 disabled:text-blue-800 disabled:shadow-none'
              : ''
          "
          :disabled="!copyButtonEnabled"
          :label="copyText"
          @click="copyKey"
        ></TextButton>
        <br />
        <div
          v-if="apiKey.valid"
          class="ds-label-02-reg-italic mt-24 text-gray-900"
        >
          gültig bis:
          {{ dayjs(apiKey.validUntil).format("DD.MM.YYYY HH:mm:ss") }}
        </div>
        <div v-else class="ds-label-02-bold mt-24 text-red-900">
          API-Key ist abgelaufen!
        </div>
      </div>
      <div v-if="apiKey.valid">
        <TextButton
          class="mt-20"
          label="Sperren"
          @click="invalidateApiKey"
        ></TextButton>
      </div>
      <div v-else>
        <TextButton
          class="mt-20"
          label="Neuen API-Schlüssel erstellen"
          @click="generateApiKey"
        ></TextButton>
      </div>
    </div>
    <div v-else>
      <TextButton
        class="mt-20"
        label="Neuen API-Schlüssel erstellen"
        @click="generateApiKey"
      ></TextButton>
    </div>
  </div>
</template>
