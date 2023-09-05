<script lang="ts" setup>
import { storeToRefs } from "pinia"
import TableOfContents from "@/components/TableOfContents.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)
</script>

<template>
  <div class="w-5/6 max-w-screen-lg">
    <h1 class="ds-label-01-bold mb-32">Nichtamtliches Inhaltsverzeichnis</h1>
    <div class="border-b border-gray-400">
      <TableOfContents
        v-if="loadedNorm?.documentation"
        :document-sections="loadedNorm?.documentation"
      />
    </div>
  </div>
</template>
