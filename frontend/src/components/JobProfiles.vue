<script lang="ts" setup>
import ChipsInput from "@/components/input/ChipsInput.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const validationStore = useValidationStore<["jobProfiles"][number]>()
</script>

<template>
  <div class="gap-0">
    <InputField
      id="jobProfileInput"
      v-slot="slotProps"
      data-testid="Berufsbild"
      :label="label"
    >
      <ChipsInput
        id="jobProfiles"
        v-model="store.documentUnit!.contentRelatedIndexing.jobProfiles"
        aria-label="Berufsbild"
        data-testid="job-profiles"
        :has-error="slotProps.hasError"
        @focus="validationStore.remove('jobProfiles')"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
  </div>
</template>
