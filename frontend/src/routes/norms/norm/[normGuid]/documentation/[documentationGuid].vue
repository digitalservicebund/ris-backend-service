<script lang="ts" setup>
import { computed } from "vue"
import { useRoute } from "vue-router"
import SingleDataFieldSection from "@/components/SingleDataFieldSection.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const route = useRoute()
const documentationGuid = computed(() =>
  route.params.documentationGuid.toString(),
)
const { findDocumentation } = useLoadedNormStore()
const documentation = findDocumentation(documentationGuid)
</script>

<template>
  <div class="flex max-w-screen-lg flex-col gap-8">
    <SingleDataFieldSection
      id="documentationMarker"
      :input-attributes="{ ariaLabel: 'Bezeichnung des Elements', rows: 1 }"
      label="Bezeichnung des Elements"
      :model-value="documentation?.marker"
    />
    <SingleDataFieldSection
      id="documentationHeading"
      :input-attributes="{ ariaLabel: 'Überschrift des Elements', rows: 1 }"
      label="Überschrift des Elements"
      :model-value="documentation?.heading"
    />
  </div>
</template>
