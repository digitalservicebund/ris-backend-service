<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

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

function createComputedProperty(key: string) {
  return computed({
    get: () => inputValue.value[key]?.[0],
    set: (data?: string) => data && (inputValue.value[key] = [data]),
  })
}

const euYear = createComputedProperty("YEAR")
const euSeries = createComputedProperty("SERIES")
const euNumber = createComputedProperty("NUMBER")
const euPage = createComputedProperty("PAGE_NUMBER")
const euInfo = createComputedProperty("ADDITIONAL_INFO")
const euExplanations = createComputedProperty("EXPLANATION")
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
      <TextInput
        id="euAnnouncementYear"
        v-model="euYear"
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
        v-model="euSeries"
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
        v-model="euNumber"
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
        v-model="euPage"
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
    <textarea
      id="euAnnouncementInfo"
      v-model="euInfo"
      aria-label="Zusatzangaben"
      class="mt-4 outline outline-2 outline-blue-900 overflow-y-auto"
      rows="4"
    />
  </InputField>
  <InputField
    id="euAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <textarea
      id="euAnnouncementExplanations"
      v-model="euExplanations"
      aria-label="Erläuterungen"
      class="mt-4 outline outline-2 outline-blue-900 overflow-y-auto"
      rows="4"
    />
  </InputField>
</template>
