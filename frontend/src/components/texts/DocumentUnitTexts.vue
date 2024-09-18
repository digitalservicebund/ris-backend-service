<script lang="ts" setup>
import DocumentUnitAdditionalTexts from "@/components/texts/DocumentUnitAdditionalTexts.vue"
import DocumentUnitLongTexts from "@/components/texts/DocumentUnitLongTexts.vue"
import DocumentUnitShortTexts from "@/components/texts/DocumentUnitShortTexts.vue"
</script>

<template>
  <div class="flex flex-col bg-white p-24">
    <h2 class="ds-subhead mb-24">Kurz- und Langtexte</h2>
    <DocumentUnitShortTexts />
    <DocumentUnitLongTexts />
    <DocumentUnitAdditionalTexts />
  </div>
</template>
