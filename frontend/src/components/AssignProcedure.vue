<script setup lang="ts">
import Button from "primevue/button"
import { ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import { Procedure } from "@/domain/procedure"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{
  assignProcedure: [procedure: Procedure | undefined]
}>()

const procedure = ref<Procedure | undefined>(undefined)
const hasNoProcedureSelectedError = ref(false)

const assignProcedures = () => {
  if (procedure.value) {
    hasNoProcedureSelectedError.value = false
  } else {
    hasNoProcedureSelectedError.value = true
  }

  emit("assignProcedure", procedure.value)
}
</script>

<template>
  <div class="my-16 flex flex-col gap-4 justify-self-end">
    <div class="flex gap-8">
      <ComboboxInput
        id="procedure"
        v-model="procedure"
        aria-label="Vorgang auswählen"
        class="min-w-[270px]"
        :has-error="hasNoProcedureSelectedError"
        :item-service="ComboboxItemService.getProcedures"
        manual-entry
        placeholder="Vorgang auswählen"
        @focus="() => (hasNoProcedureSelectedError = false)"
      ></ComboboxInput>
      <Button
        aria-label="Zu Vorgang hinzufügen"
        class="whitespace-nowrap"
        label="Zu Vorgang hinzufügen"
        severity="secondary"
        @click="assignProcedures"
      />
    </div>
    <InputErrorMessages
      v-if="hasNoProcedureSelectedError"
      class="self-start"
      error-message="Wählen Sie einen Vorgang aus"
    />
  </div>
</template>
