<script lang="ts" setup>
import { storeToRefs } from "pinia"
import TableOfContents from "@/components/TableOfContents.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)
</script>

<template>
  <div class="max-w-screen-lg">
    <h2 class="ds-label-01-bold mb-44">Nichtamtliches Inhaltsverzeichnis</h2>
    <TableOfContents :document-sections="loadedNorm?.documentation" />
  </div>
</template>
