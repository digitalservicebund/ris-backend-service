<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { computed } from "vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()

const jobProfiles = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.jobProfiles ?? [],
  set: (newValue) => {
    store.documentUnit!.contentRelatedIndexing.jobProfiles = newValue
  },
})
</script>

<template>
  <div class="gap-0">
    <div class="ris-label2-regular mb-4" data-testid="Berufsbild">
      {{ label }}
    </div>
    <RisChipsInput
      id="jobProfiles"
      v-model="jobProfiles"
      aria-label="Berufsbild"
    />
  </div>
</template>
