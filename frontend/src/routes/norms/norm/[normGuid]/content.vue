<script lang="ts" setup>
import { storeToRefs } from "pinia"
import TableOfContents from "@/components/tableOfContents/TableOfContents.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const loadedNormStore = useLoadedNormStore()
const { loadedNorm } = storeToRefs(loadedNormStore)
</script>

<template>
  <div class="w-5/6 max-w-screen-lg">
    <h1 class="ds-heading-02-reg mb-32">
      {{
        loadedNorm?.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0] ?? ""
      }}
    </h1>
    <h1 class="ds-label-01-bold mb-32">Nichtamtliches Inhaltsverzeichnis</h1>

    <TableOfContents
      v-if="loadedNorm?.documentation"
      :document-sections="loadedNorm?.documentation"
      :has-conclusion="!!loadedNorm?.conclusion"
      :has-formula="!!loadedNorm?.formula"
      :norm-guid="loadedNorm?.guid"
      :recitals="loadedNorm?.recitals"
    />
  </div>
</template>
