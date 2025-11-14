<script lang="ts" setup>
import InputText from "primevue/inputtext"
import { ValidationError } from "@/components/input/types"
import documentUnitService from "@/services/documentUnitService"

const emit = defineEmits<{
  "update:validationError": [value?: ValidationError]
}>()

const model = defineModel<string>()

async function validate() {
  if (model.value) {
    const response = await documentUnitService.validateSingleNorm({
      singleNorm: model.value,
    })

    if (response.data === "Ok") {
      emit("update:validationError")
    } else {
      emit("update:validationError", {
        message: "Inhalt nicht valide",
        instance: "norm",
      })
    }
  } else {
    emit("update:validationError")
  }
}
</script>

<template>
  <InputText
    v-model="model"
    aria-label="Tarifnorm"
    class="w-full"
    size="small"
    @blur="validate"
  ></InputText>
</template>
