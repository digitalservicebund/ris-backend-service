<script setup lang="ts">
import Button from "primevue/button"
import { ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import { Procedure } from "@/domain/procedure"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{ assignProcedure: [procedure: Procedure] }>()

const procedure = ref<Procedure | undefined>(undefined)
const hasNoProcedureSelectedError = ref(false)

const assignProcedures = () => {
  if (!procedure.value) {
    hasNoProcedureSelectedError.value = true
  } else {
    hasNoProcedureSelectedError.value = false
    emit("assignProcedure", procedure.value)
  }
}
</script>

<template>
  <div
    class="my-16 grid grid-cols-[270px_auto] grid-rows-[auto_auto] gap-x-8 gap-y-4 [grid-template-areas:'input_button''errors_errors']"
  >
    <ComboboxInput
      id="procedure"
      v-model="procedure"
      aria-label="Vorgang auswählen"
      class="[grid-area:input]"
      :has-error="hasNoProcedureSelectedError"
      :item-service="ComboboxItemService.getProcedures"
      manual-entry
      placeholder="Vorgang auswählen"
    ></ComboboxInput>
    <Button
      aria-label="Zu Vorgang hinzufügen"
      class="[grid-area:button]"
      label="Zu Vorgang hinzufügen"
      severity="secondary"
      @click="assignProcedures"
    />
    <InputErrorMessages
      v-if="hasNoProcedureSelectedError"
      class="[grid-area:errors]"
      error-message="Wählen Sie einen Vorgang aus"
    />
  </div>
</template>
