<script setup lang="ts">
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const { loadedNorm } = useLoadedNormStore()
</script>

<template>
  <div>
    <SingleDataFieldSection
      id="formulaText"
      :input-attributes="{ ariaLabel: 'Text', autosize: true }"
      label="Text"
      :model-value="loadedNorm?.recitals?.text"
    />
  </div>
</template>
