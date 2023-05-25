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

const defaultValueEuGovernmentGazette = "Amtsblatt der EU"

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data?: string) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const series = computed({
  get: () => inputValue.value.SERIES?.[0],
  set: (data?: string) => (inputValue.value.SERIES = data ? [data] : undefined),
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
  <div class="w-full">
    <InputField
      id="euAnnouncementGazette"
      aria-label="Amtsblatt der EU"
      label="Amtsblatt der EU"
    >
      <TextInput
        id="euAnnouncementGazette"
        alt-text="Amtsblatt der EU"
        aria-label="Amtsblatt der EU"
        class="border-gray-800 read-only:!border-solid"
        read-only
        :value="defaultValueEuGovernmentGazette"
      />
    </InputField>
  </div>
  <div class="flex gap-16 justify-between">
    <InputField
      id="euAnnouncementYear"
      aria-label="Jahresangabe"
      class="w-full"
      label="Jahresangabe"
    >
      <YearInput
        id="euAnnouncementYear"
        v-model="year"
        alt-text="Jahresangabe"
        aria-label="Jahresangabe"
      />
    </InputField>
    <InputField
      id="euAnnouncementSeries"
      aria-label="Reihe"
      class="w-full"
      label="Reihe"
    >
      <TextInput
        id="euAnnouncementSeries"
        v-model="series"
        alt-text="Reihe"
        aria-label="Reihe"
      />
    </InputField>
    <InputField
      id="euAnnouncementNumber"
      aria-label="Nummer des Amtsblatts"
      class="w-full"
      label="Nummer des Amtsblatts"
    >
      <TextInput
        id="euAnnouncementNumber"
        v-model="number"
        alt-text="Nummer des Amtsblatts"
        aria-label="Nummer des Amtsblatts"
      />
    </InputField>
    <InputField
      id="euAnnouncementPage"
      aria-label="Seitenzahl"
      class="w-full"
      label="Seitenzahl"
    >
      <TextInput
        id="euAnnouncementPage"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <InputField
    id="euAnnouncementInfo"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <TextInput
      id="euAnnouncementInfo"
      v-model="additionalInfo"
      aria-label="Zusatzangaben"
    />
  </InputField>
  <InputField
    id="euAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <TextInput
      id="euAnnouncementExplanations"
      v-model="explanation"
      aria-label="Erläuterungen"
    />
  </InputField>
</template>
