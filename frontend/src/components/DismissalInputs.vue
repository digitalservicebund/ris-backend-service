<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { computed } from "vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const dismissalTypes = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.dismissalTypes ?? [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.dismissalTypes = newValues
  },
})
const dismissalGrounds = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.dismissalGrounds ?? [],
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.dismissalGrounds = newValues
  },
})
</script>

<template>
  <div class="flex flex-row gap-24">
    <div class="basis-1/2 gap-0">
      <div class="ris-label2-regular mb-4" data-testid="dismissal-types">
        Kündigungsarten
      </div>
      <RisChipsInput
        id="dismissalTypes"
        v-model="dismissalTypes"
        aria-label="Kündigungsarten"
      />
    </div>
    <div class="basis-1/2 gap-0">
      <div class="ris-label2-regular mb-4" data-testid="dismissal-grounds">
        Kündigungsgründe
      </div>
      <RisChipsInput
        id="dismissalGrounds"
        v-model="dismissalGrounds"
        aria-label="Kündigungsgründe"
      />
    </div>
  </div>
</template>
