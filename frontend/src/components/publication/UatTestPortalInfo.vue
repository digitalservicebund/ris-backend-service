<script lang="ts" setup>
import { computed } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import useSessionStore from "@/stores/sessionStore"

const session = useSessionStore()
const uatLink = computed(() => {
  return { url: session.env?.portalUrl, displayText: "Link zum UAT-Portal" }
})
const isUat = computed(() => session.env?.environment === "uat")
</script>
<template>
  <InfoModal
    v-if="isUat"
    aria-label="Information Testportal in UAT"
    class="mt-8"
    description="Dies ist zugänglich unter:&nbsp;"
    :status="InfoStatus.INFO"
    title="UAT veröffentlicht Dokeinheiten in ein Testportal, nicht das öffentliche Portal."
  >
    <template #link>
      <a
        class="ris-link2-regular whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
        :href="uatLink.url"
        rel="noopener noreferrer"
        target="_blank"
      >
        {{ uatLink.displayText }}
      </a>
    </template>
  </InfoModal>
</template>
