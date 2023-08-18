<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
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
    <div class="mb-8 flex justify-between">
      <InputField
        :id="`${idPrefix}TypeDate`"
        v-slot="{ id }"
        label="Datum"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          :name="`${idPrefix}InputType`"
          size="medium"
          :value="InputType.DATE"
        />
      </InputField>

      <InputField
        :id="`${idPrefix}TypeYear`"
        v-slot="{ id }"
        label="Jahresangabe"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          :name="`${idPrefix}InputType`"
          size="medium"
          :value="InputType.YEAR"
        />
      </InputField>
    </div>

    <InputField
      :id="
        selectedInputType === InputType.DATE
          ? `${idPrefix}Date`
          : `${idPrefix}Year`
      "
      v-slot="{ id, hasError, updateValidationError }"
      class="mb-0"
      :label="label"
      :label-position="LabelPosition.TOP"
    >
      <DateInput
        v-if="selectedInputType === InputType.DATE"
        :id="id"
        v-model="dateValue"
        :aria-label="label"
        :has-error="hasError"
        is-future-date
        @update:validation-error="updateValidationError"
      />
      <YearInput
        v-else-if="selectedInputType === InputType.YEAR"
        :id="id"
        v-model="yearValue"
        :aria-label="`${label} Jahresangabe`"
        :has-error="hasError"
        @update:validation-error="updateValidationError"
      />
    </InputField>
  </div>
</template>
