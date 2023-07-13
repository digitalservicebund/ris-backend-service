<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputElement from "@/shared/components/input/InputElement.vue"
import { InputType } from "@/shared/components/input/types"
import YearInput from "@/shared/components/input/YearInput.vue"

interface Props {
  modelValue: Metadata
  idPrefix: string
  label: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.DATE)
function detectSelectedInputType(): void {
  if (inputValue.value.YEAR) {
    selectedInputType.value = InputType.YEAR
  } else selectedInputType.value = InputType.DATE
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true },
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(inputValue, detectSelectedInputType, { immediate: true, deep: true })

const dateValue = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (value) => {
    inputValue.value.DATE = value ? [value] : undefined
    inputValue.value.YEAR = undefined
  },
})

const yearValue = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (value) => {
    inputValue.value.YEAR = value ? [value] : []
    inputValue.value.DATE = undefined
  },
})
</script>

<template>
  <div class="w-320">
    <div class="flex justify-between mb-24">
      <label class="form-control">
        <input
          :id="`${idPrefix}TypeDate`"
          v-model="selectedInputType"
          :aria-label="`Wählen Sie ${label} Datum`"
          :name="`${idPrefix}InputType`"
          type="radio"
          :value="InputType.DATE"
        />
        Datum
      </label>
      <label class="form-control">
        <input
          :id="`${idPrefix}TypeYear`"
          v-model="selectedInputType"
          :aria-label="`Wählen Sie ${label} Jahr`"
          :name="`${idPrefix}InputType`"
          type="radio"
          :value="InputType.YEAR"
        />
        Jahresangabe
      </label>
    </div>
    <label
      class="flex gap-4 items-center label-03-reg mb-2 text-gray-900"
      :for="
        selectedInputType === InputType.DATE
          ? `${idPrefix}Date`
          : `${idPrefix}Year`
      "
      >{{ label }}</label
    >
    <InputElement
      v-if="selectedInputType === InputType.DATE"
      :id="`${idPrefix}Date`"
      v-model="dateValue"
      :alt-text="`${label} Datum`"
      :attributes="{ ariaLabel: `${label} Datum` }"
      :type="InputType.DATE"
    />
    <div>
      <YearInput
        v-if="selectedInputType === InputType.YEAR"
        :id="`${idPrefix}Year`"
        v-model="yearValue"
        :aria-label="`${label} Jahresangabe`"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]:hover,
input[type="radio"]:focus {
  border: 4px solid #004b76;
  outline: none;
}

input[type="radio"]::before {
  width: 0.9em;
  height: 0.9em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
