<script lang="ts" setup>
import InputText from "primevue/inputtext"
import { ValidationError } from "@/components/input/types"

const emit = defineEmits<{
  "update:validationError": [value?: ValidationError]
}>()

const model = defineModel<string>()

const DATE_PATTERN = /^((([0-2][1-9]|3[0-1])\.)?(0[1-9]|1[0-2])\.)?\d{4}$/

async function validate() {
  if (model.value && !model.value.match(DATE_PATTERN)) {
    emit("update:validationError", {
      message:
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      instance: "date",
    })
  } else {
    emit("update:validationError")
  }
}
</script>

<template>
  <InputText
    v-model="model"
    aria-label="Datum"
    class="w-full"
    placeholder="TT.MM.JJJJ, MM.JJJJ, JJJJ"
    size="small"
    @blur="validate"
  ></InputText>
</template>
