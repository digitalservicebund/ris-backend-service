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

const link = computed({
  get: () => inputValue.value.LINK?.[0],
  set: (data?: string) => (inputValue.value.LINK = data ? [data] : undefined),
})

const relatedData = computed({
  get: () => inputValue.value.RELATED_DATA?.[0],
  set: (data?: string) =>
    (inputValue.value.RELATED_DATA = data ? [data] : undefined),
})

const note = computed({
  get: () => inputValue.value.EXTERNAL_DATA_NOTE?.[0],
  set: (data?: string) =>
    (inputValue.value.EXTERNAL_DATA_NOTE = data ? [data] : undefined),
})

const appendix = computed({
  get: () => inputValue.value.APPENDIX?.[0],
  set: (data?: string) =>
    (inputValue.value.APPENDIX = data ? [data] : undefined),
})

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <InputField id="digitalEvidenceLink" label="Verlinkung">
      <TextInput
        id="digitalEvidenceLink"
        v-model="link"
        aria-label="Verlinkung"
      />
    </InputField>

    <InputField id="digitalEvidenceRelatedData" label="Zugehörige Daten">
      <TextInput
        id="digitalEvidenceRelatedData"
        v-model="relatedData"
        aria-label="Zugehörige Daten"
      />
    </InputField>

    <InputField
      id="digitalEvidenceExternalDataNote"
      label="Hinweis auf fremde Verlinkung oder Daten"
    >
      <TextInput
        id="digitalEvidenceExternalDataNote"
        v-model="note"
        aria-label="Hinweis auf fremde Verlinkung oder Daten"
      />
    </InputField>

    <InputField id="digitalEvidenceAppendix" label="Zusatz zum Nachweis">
      <TextInput
        id="digitalEvidenceAppendix"
        v-model="appendix"
        aria-label="Zusatz zum Nachweis"
      />
    </InputField>
  </div>
</template>
