<script lang="ts" setup>
import Message from "primevue/message"
import { computed } from "vue"
import useSessionStore from "@/stores/sessionStore"

const session = useSessionStore()
const uatLink = computed(() => {
  return { url: session.env?.portalUrl, displayText: "Link zum UAT-Portal" }
})
const isUat = computed(() => session.env?.environment === "uat")
</script>
<template>
  <Message
    v-if="isUat"
    aria-label="Information Testportal in UAT"
    class="mt-8"
    severity="info"
  >
    <p class="ris-body1-bold">
      UAT veröffentlicht Dokeinheiten in ein Testportal, nicht das öffentliche
      Portal.
    </p>
    <p>
      Dies ist zugänglich unter:&nbsp;
      <a
        class="ris-link2-regular whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
        :href="uatLink.url"
        rel="noopener noreferrer"
        target="_blank"
      >
        {{ uatLink.displayText }}
      </a>
    </p>
  </Message>
</template>
