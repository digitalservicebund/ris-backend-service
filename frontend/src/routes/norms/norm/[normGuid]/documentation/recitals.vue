<script setup lang="ts">
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const { loadedNorm } = useLoadedNormStore()
</script>

<template>
  <div>
    <SingleDataFieldSection
      id="recitalsMarker"
      :input-attributes="{ ariaLabel: 'Bezeichnung des Elements', rows: 1 }"
      label="Bezeichnung des Elements"
      :model-value="loadedNorm?.recitals?.marker"
    />
    <SingleDataFieldSection
      id="recitalsHeading"
      :input-attributes="{ ariaLabel: 'Überschrift des Elements', rows: 1 }"
      label="Überschrift des Elements"
      :model-value="loadedNorm?.recitals?.heading"
    />
    <SingleDataFieldSection
      id="recitalsText"
      :input-attributes="{ ariaLabel: 'Text', autosize: true }"
      label="Text"
      :model-value="loadedNorm?.recitals?.text"
    />
  </div>
</template>
