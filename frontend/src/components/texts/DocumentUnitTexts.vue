<script lang="ts" setup>
import DocumentUnitAdditionalTexts from "@/components/texts/DocumentUnitAdditionalTexts.vue"
import DocumentUnitLongTexts from "@/components/texts/DocumentUnitLongTexts.vue"
import DocumentUnitShortTexts from "@/components/texts/DocumentUnitShortTexts.vue"
import { useInternalUser } from "@/composables/useInternalUser"

const isInternalUser = useInternalUser()
</script>

<template>
  <div class="flex flex-col bg-white px-24">
    <h2 class="ds-subhead my-24">Kurz- & Langtexte</h2>
    <DocumentUnitShortTexts />
    <DocumentUnitLongTexts v-if="isInternalUser" />
    <DocumentUnitAdditionalTexts v-if="isInternalUser" />
  </div>
</template>
