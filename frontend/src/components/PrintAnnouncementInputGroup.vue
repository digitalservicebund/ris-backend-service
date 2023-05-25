<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const announcementGazette = computed({
  get: () => inputValue.value.ANNOUNCEMENT_GAZETTE?.[0],
  set: (data?: string) =>
    (inputValue.value.ANNOUNCEMENT_GAZETTE = data ? [data] : undefined),
})

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data?: string) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const number = computed({
  get: () => inputValue.value.NUMBER?.[0],
  set: (data?: string) => (inputValue.value.NUMBER = data ? [data] : undefined),
})

const pageNumber = computed({
  get: () => inputValue.value.PAGE?.[0],
  set: (data?: string) => (inputValue.value.PAGE = data ? [data] : undefined),
})

const additionalInfo = computed({
  get: () => inputValue.value.ADDITIONAL_INFO?.[0],
  set: (data?: string) =>
    (inputValue.value.ADDITIONAL_INFO = data ? [data] : undefined),
})

const explanation = computed({
  get: () => inputValue.value.EXPLANATION?.[0],
  set: (data?: string) =>
    (inputValue.value.EXPLANATION = data ? [data] : undefined),
})
</script>
<template>
  <div class="flex ful-w gap-16 justify-between">
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
      aria-label="Jahr"
      class="md:w-auto"
      label="Jahr"
    >
      <YearInput
        id="printAnnouncementYear"
        v-model="year"
        alt-text="Jahr"
        aria-label="Jahr"
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
  <div>
    <InputField
      id="printAnnouncementInfo"
      aria-label="Zusatzangaben"
      label="Zusatzangaben"
    >
      <TextInput
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
      <TextInput
        id="printAnnouncementExplanations"
        v-model="explanation"
        aria-label="Erläuterungen"
      />
    </InputField>
  </div>
</template>
