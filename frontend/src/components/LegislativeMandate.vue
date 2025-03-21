<script lang="ts" setup>
import Checkbox from "primevue/checkbox"
import { computed } from "vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
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
      <InputField
        id="legislativeMandate"
        v-slot="{ id }"
        :label="label"
        label-class="ds-label-01-reg"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          :id="id"
          v-model="hasLegislativeMandate"
          aria-label="Gesetzgebungsauftrag"
          binary
          data-testid="legislative-mandate"
          size="large"
        />
      </InputField>
    </div>
  </div>
</template>
