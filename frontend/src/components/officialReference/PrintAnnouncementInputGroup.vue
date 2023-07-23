<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"
import YearInput from "@/shared/components/input/YearInput.vue"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const inputValue = ref(props.modelValue)
const validationErrors = ref<ValidationError[]>()

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

const announcementGazette = computed({
  get: () => inputValue.value.ANNOUNCEMENT_GAZETTE?.[0],
  set: (data) =>
    (inputValue.value.ANNOUNCEMENT_GAZETTE = data ? [data] : undefined),
})

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const number = computed({
  get: () => inputValue.value.NUMBER?.[0],
  set: (data) => (inputValue.value.NUMBER = data ? [data] : undefined),
})

const pageNumber = computed({
  get: () => inputValue.value.PAGE?.[0],
  set: (data) => (inputValue.value.PAGE = data ? [data] : undefined),
})

const additionalInfo = computed({
  get: () => inputValue.value.ADDITIONAL_INFO,
  set: (data) => (inputValue.value.ADDITIONAL_INFO = data),
})

const explanation = computed({
  get: () => inputValue.value.EXPLANATION,
  set: (data) => (inputValue.value.EXPLANATION = data),
})
</script>
<template>
  <div class="ful-w flex justify-between gap-16">
    <InputField
      id="printAnnouncementGazette"
      aria-label="Verkündungsblatt"
      class="w-1/3"
      label="Verkündungsblatt"
    >
      <TextInput
        id="printAnnouncementGazette"
        v-model="announcementGazette"
        alt-text="Verkündungsblatt"
        aria-label="Verkündungsblatt"
      />
    </InputField>
    <InputField
      id="printAnnouncementYear"
      v-slot="slotProps"
      aria-label="Jahr"
      class="md:w-auto"
      label="Jahr"
      :validation-error="
        validationErrors?.find((err) => err.field === 'printAnnouncementYear')
      "
    >
      <YearInput
        id="printAnnouncementYear"
        v-model="year"
        alt-text="Jahr"
        aria-label="Jahr"
        :has-error="slotProps.hasError"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
    <InputField
      id="printAnnouncementNumber"
      aria-label="Nummer"
      class="md:w-auto"
      label="Nummer"
    >
      <TextInput
        id="printAnnouncementNumber"
        v-model="number"
        alt-text="Nummer"
        aria-label="Nummer"
      />
    </InputField>
    <InputField
      id="printAnnouncementPage"
      aria-label="Seitenzahl"
      class="md:w-auto"
      label="Seitenzahl"
    >
      <TextInput
        id="printAnnouncementPage"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <InputField
    id="printAnnouncementInfo"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <ChipsInput
      id="printAnnouncementInfo"
      v-model="additionalInfo"
      aria-label="Zusatzangaben"
    />
  </InputField>

  <InputField
    id="printAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <ChipsInput
      id="printAnnouncementExplanations"
      v-model="explanation"
      aria-label="Erläuterungen"
    />
  </InputField>
</template>
