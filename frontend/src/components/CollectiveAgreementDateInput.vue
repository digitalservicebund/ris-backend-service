<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import InputText from "primevue/inputtext"
import { computed, nextTick } from "vue"
import { ValidationError } from "@/components/input/types"

const emit = defineEmits<{
  "update:validationError": [value?: ValidationError]
}>()

const model = defineModel<string>()

const isValidDate = computed(() => {
  return dayjs(model.value, ["YYYY", "MM.YYYY", "DD.MM.YYYY"], true).isValid()
})
const isInPast = computed(() => {
  return dayjs(model.value, ["YYYY", "MM.YYYY", "DD.MM.YYYY"], true).isBefore(
    dayjs(),
  )
})

dayjs.extend(customParseFormat)

async function validate() {
  await nextTick()

  if (model.value && !isValidDate.value) {
    emit("update:validationError", {
      message:
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      instance: "date",
    })
    return
  }
  if (model.value && !isInPast.value) {
    emit("update:validationError", {
      message: "Das Datum darf nicht in der Zukunft liegen",
      instance: "date",
    })
    return
  }
  emit("update:validationError")
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
