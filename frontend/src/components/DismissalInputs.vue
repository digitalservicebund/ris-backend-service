<script lang="ts" setup>
import { computed } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
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
const validationStore =
  useValidationStore<["dismissalTypes", "dismissalGrounds"][number]>()
</script>

<template>
  <div class="flex flex-row gap-24">
    <div class="basis-1/2 gap-0">
      <InputField
        id="dismissalTypes"
        v-slot="slotProps"
        label="Kündigungsarten"
      >
        <ChipsInput
          :id="slotProps.id"
          v-model="dismissalTypes"
          aria-label="Kündigungsarten"
          data-testid="dismissal-types"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('dismissalTypes')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <div class="basis-1/2 gap-0">
      <InputField
        id="dismissalGrounds"
        v-slot="slotProps"
        label="Kündigungsgründe"
      >
        <ChipsInput
          :id="slotProps.id"
          v-model="dismissalGrounds"
          aria-label="Kündigungsgründe"
          data-testid="dismissal-grounds"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('dismissalGrounds')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
  </div>
</template>
