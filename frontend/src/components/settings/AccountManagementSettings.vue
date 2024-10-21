<script setup lang="ts">
import { sanitizeUrl } from "@braintree/sanitize-url"
import { computed, onMounted, ref } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextButton from "@/components/input/TextButton.vue"
import adminService from "@/services/adminService"

const link = ref<string | undefined>(undefined)
const sanitizedUrl = computed(() => sanitizeUrl(link.value))

async function fetchUrl() {
  link.value = (await adminService.getAccountManagementUrl()).data
}

onMounted(() => {
  fetchUrl()
})
</script>

<template>
  <FlexContainer v-if="link" flex-direction="flex-col">
    <span class="ds-label-02-bold">Kontoverwaltung</span>
    <a aria-label="Bare.ID öffnen" :href="sanitizedUrl" target="_blank">
      <TextButton class="mt-16" label="Bare.ID öffnen"> </TextButton>
    </a>
  </FlexContainer>
</template>
