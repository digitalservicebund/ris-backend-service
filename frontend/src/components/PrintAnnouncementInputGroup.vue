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

const printGazette = createComputedProperty("ANNOUNCEMENT_GAZETTE")
const printYear = createComputedProperty("YEAR")
const printNumber = createComputedProperty("NUMBER")
const printPage = createComputedProperty("PAGE_NUMBER")
const printInfo = createComputedProperty("ADDITIONAL_INFO")
const printExplanations = createComputedProperty("EXPLANATION")
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
        v-model="printGazette"
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
      <TextInput
        id="printAnnouncementYear"
        v-model="printYear"
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
        v-model="printNumber"
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
        v-model="printPage"
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
      <textarea
        id="printAnnouncementInfo"
        v-model="printInfo"
        aria-label="Zusatzangaben"
        class="mt-4 outline outline-2 outline-blue-900 overflow-y-auto"
        rows="4"
      />
    </InputField>

    <InputField
      id="printAnnouncementExplanations"
      aria-label="Erläuterungen"
      label="Erläuterungen"
    >
      <textarea
        id="printAnnouncementExplanations"
        v-model="printExplanations"
        aria-label="Erläuterungen"
        class="mt-4 outline outline-2 outline-blue-900 overflow-y-auto"
        rows="4"
      />
    </InputField>
  </div>
</template>
