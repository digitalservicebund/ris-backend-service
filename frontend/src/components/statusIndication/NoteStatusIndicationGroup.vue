<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, MetadataSectionName } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"

interface Props {
  modelValue: Metadata
  type: MetadataSectionName.REPEAL | MetadataSectionName.OTHER_STATUS
}

const props = withDefaults(defineProps<Props>(), {
  type: MetadataSectionName.REPEAL,
})

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const inputValue = ref(props.modelValue)

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

const text = computed({
  get: () => inputValue.value.TEXT?.[0],
  set: (data?: string) => (inputValue.value.TEXT = data ? [data] : undefined),
})

const note = computed({
  get: () => inputValue.value.NOTE?.[0],
  set: (data?: string) => (inputValue.value.NOTE = data ? [data] : undefined),
})

const inputFields = computed(() => {
  const textOrNoteField =
    props.type === MetadataSectionName.REPEAL
      ? {
          id: "repealText",
          label: "Aufhebung",
          modelValue: text.value ?? "",
          updateModelValue: (value: string) => (text.value = value),
        }
      : {
          id: "otherStatusNote",
          label: "Sonstiger Hinweis",
          modelValue: note.value ?? "",
          updateModelValue: (value: string) => (note.value = value),
        }

  return { textOrNoteField }
})
</script>

<template>
  <div class="flex ful-w gap-16 justify-between">
    <InputField
      :id="inputFields.textOrNoteField.id"
      :aria-label="inputFields.textOrNoteField.label"
      :label="inputFields.textOrNoteField.label"
    >
      <TextAreaInput
        :id="inputFields.textOrNoteField.id"
        :aria-label="inputFields.textOrNoteField.label"
        autosize
        :model-value="inputFields.textOrNoteField.modelValue"
        @update:model-value="inputFields.textOrNoteField.updateModelValue"
      />
    </InputField>
  </div>
</template>
