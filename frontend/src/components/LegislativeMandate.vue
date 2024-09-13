<script lang="ts" setup>
import { computed } from "vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  headline: string
  label: string
}>()

const store = useDocumentUnitStore()

const hasLegislativeMandate = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.hasLegislativeMandate,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.hasLegislativeMandate = newValues
  },
})
</script>

<template>
  <div class="gap-0">
    <div class="ds-label-02-reg mb-16">{{ headline }}</div>
    <div class="flex flex-row">
      <CheckboxInput
        v-model="hasLegislativeMandate"
        aria-label="Gesetzgebungsauftrag"
        data-testid="legislative-mandate"
        size="small"
      />
      <div class="ds-label-01-reg ml-12 content-center">{{ label }}</div>
    </div>
  </div>
</template>
