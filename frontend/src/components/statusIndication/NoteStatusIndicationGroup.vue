<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata, MetadataSectionName } from "@/domain/norm"
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

const text = computed({
  get: () => props.modelValue.TEXT?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.TEXT = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const note = computed({
  get: () => props.modelValue.NOTE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.NOTE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
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
  <div class="ful-w flex justify-between gap-16">
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
