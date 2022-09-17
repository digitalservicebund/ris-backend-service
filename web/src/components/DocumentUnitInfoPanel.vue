<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnit from "../domain/documentUnit"

const props = defineProps<{ documentUnit: DocumentUnit }>()

const entries = computed(() => [
  {
    label: "Aktenzeichen",
    value: props.documentUnit.coreData.fileNumber,
  },
  {
    label: "Entscheidungsdatum",
    value: props.documentUnit.coreData.decisionDate,
  },
  {
    label: "Gerichtstyp",
    value: props.documentUnit.coreData.courtType,
  },
])
</script>

<template>
  <div
    class="bg-blue-200 border-b border-gray-400 border-solid flex items-center p-12"
  >
    <div class="grow text-30">
      {{ documentUnit.documentnumber || " - " }}
    </div>

    <div v-for="entry in entries" :key="entry.label" class="grow">
      {{ entry.label }}
      <span class="font-bold ml-2">{{ entry.value ?? "-" }}</span>
    </div>
  </div>
</template>
