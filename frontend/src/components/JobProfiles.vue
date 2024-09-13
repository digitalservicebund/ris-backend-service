<script lang="ts" setup>
import { computed } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()

const jobProfiles = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.jobProfiles,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.jobProfiles = newValues
  },
})
</script>

<template>
  <div class="gap-0">
    <div class="ds-label-02-reg mb-4" data-testId="job-profiles">
      {{ label }}
    </div>
    <ChipsInput
      id="jobProfiles"
      v-model="jobProfiles"
      aria-label="Berufsbild"
    ></ChipsInput>
  </div>
</template>
