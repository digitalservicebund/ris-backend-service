<script lang="ts" setup>
import { computed } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const dismissalTypes = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.dismissalTypes,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.dismissalTypes = newValues
  },
})
const dismissalGrounds = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.dismissalGrounds,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.dismissalGrounds = newValues
  },
})
</script>

<template>
  <div class="flex flex-row gap-24">
    <div class="basis-1/2 gap-0">
      <div class="ris-label2-regular mb-4">Kündigungsarten</div>
      <ChipsInput
        id="dismissalTypes"
        v-model="dismissalTypes"
        aria-label="Kündigungsarten"
        data-testid="dismissal-types"
      />
    </div>
    <div class="basis-1/2 gap-0">
      <div class="ris-label2-regular mb-4">Kündigungsgründe</div>
      <ChipsInput
        id="dismissalGrounds"
        v-model="dismissalGrounds"
        aria-label="Kündigungsgründe"
        data-testid="dismissal-grounds"
      />
    </div>
  </div>
</template>
