<script lang="ts" setup>
import { storeToRefs } from "pinia"
import TableOfContents from "@/components/TableOfContents.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)
</script>

<template>
  <div class="w-5/6 max-w-screen-lg">
    <h2 class="ds-label-01-bold mb-32">Nichtamtliches Inhaltsverzeichnis</h2>
    <TableOfContents
      v-if="loadedNorm?.documentation"
      :document-sections="loadedNorm?.documentation"
      :margin-left="0"
    />
  </div>
</template>
