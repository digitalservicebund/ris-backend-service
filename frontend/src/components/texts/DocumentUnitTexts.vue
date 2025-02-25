<script lang="ts" setup>
import type { Component } from "vue"
import DocumentUnitAdditionalTexts from "@/components/texts/DocumentUnitAdditionalTexts.vue"
import DocumentUnitLongTexts from "@/components/texts/DocumentUnitLongTexts.vue"
import DocumentUnitShortTexts from "@/components/texts/DocumentUnitShortTexts.vue"
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
    <DocumentUnitShortTexts v-bind="{ registerTextEditorRef }" />
    <DocumentUnitLongTexts
      v-if="isInternalUser"
      v-bind="{ registerTextEditorRef }"
    />
    <DocumentUnitAdditionalTexts
      v-if="isInternalUser"
      v-bind="{ registerTextEditorRef }"
    />
  </div>
</template>
