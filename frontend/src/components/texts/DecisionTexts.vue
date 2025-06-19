<script lang="ts" setup>
import type { Component } from "vue"
import DecisionAdditionalTexts from "@/components/texts/DecisionAdditionalTexts.vue"
import DecisionLongTexts from "@/components/texts/DecisionLongTexts.vue"
import DecisionShortTexts from "@/components/texts/DecisionShortTexts.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useInternalUser } from "@/composables/useInternalUser"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const isInternalUser = useInternalUser()
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Kurz- & Langtexte</TitleElement>
    <DecisionShortTexts v-bind="{ registerTextEditorRef }" />
    <DecisionLongTexts
      v-if="isInternalUser"
      v-bind="{ registerTextEditorRef }"
    />
    <DecisionAdditionalTexts
      v-if="isInternalUser"
      v-bind="{ registerTextEditorRef }"
    />
  </div>
</template>
