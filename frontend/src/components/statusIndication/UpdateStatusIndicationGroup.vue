<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, MetadataSectionName } from "@/domain/Norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
  type?: MetadataSectionName.STATUS | MetadataSectionName.REISSUE
}

const props = withDefaults(defineProps<Props>(), {
  type: MetadataSectionName.STATUS,
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
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const note = computed({
  get: () => inputValue.value.NOTE?.[0],
  set: (data?: string) => (inputValue.value.NOTE = data ? [data] : undefined),
})

const description = computed({
  get: () => inputValue.value.DESCRIPTION?.[0],
  set: (data?: string) =>
    (inputValue.value.DESCRIPTION = data ? [data] : undefined),
})

const article = computed({
  get: () => inputValue.value.ARTICLE?.[0],
  set: (data?: string) =>
    (inputValue.value.ARTICLE = data ? [data] : undefined),
})

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data?: string) => (inputValue.value.DATE = data ? [data] : undefined),
})

const singleReference = computed({
  get: () => inputValue.value.REFERENCE?.[0],
  set: (data?: string) =>
    (inputValue.value.REFERENCE = data ? [data] : undefined),
})

const multipleReferences = computed({
  get: () => inputValue.value.REFERENCE,
  set: (data?: string[]) =>
    (inputValue.value.REFERENCE = data ? data : undefined),
})

const inputFields = computed(() => {
  const noteField =
    props.type === MetadataSectionName.STATUS
      ? {
          id: "statusNote",
          label: "Änderungshinweis",
          modelValue: note.value ?? "",
          updateModelValue: (value: string) => (note.value = value),
        }
      : {
          id: "reissueNote",
          label: "Neufassungshinweis",
          modelValue: note.value ?? "",
          updateModelValue: (value: string) => (note.value = value),
        }

  const descriptionOrArticleField =
    props.type === MetadataSectionName.STATUS
      ? {
          id: "statusDescription",
          label: "Bezeichnung der Änderungsvorschrift",
          modelValue: description.value ?? "",
          updateModelValue: (value: string) => (description.value = value),
        }
      : {
          id: "reissueArticle",
          label: "Bezeichnung der Bekanntmachung",
          modelValue: article.value ?? "",
          updateModelValue: (value: string) => (article.value = value),
        }

  const dateField =
    props.type === MetadataSectionName.STATUS
      ? {
          id: "statusDate",
          label: "Datum der Änderungsvorschrift",
          modelValue: date.value ?? "",
          updateModelValue: (value: string) => (date.value = value),
        }
      : {
          id: "reissueDate",
          label: "Datum der Bekanntmachung",
          modelValue: date.value ?? "",
          updateModelValue: (value: string) => (date.value = value),
        }

  const referenceField =
    props.type === MetadataSectionName.STATUS
      ? {
          id: "statusReference",
          label: "Fundstellen der Änderungsvorschrift",
          modelValue: multipleReferences.value ?? [],
          updateModelValue: (value: string[]) =>
            (multipleReferences.value = value),
          multi: true,
        }
      : {
          id: "reissueReference",
          label: "Fundstelle der Bekanntmachung",
          modelValue: singleReference.value ?? "",
          updateModelValue: (value: string) => (singleReference.value = value),
          multi: false,
        }

  return { noteField, descriptionOrArticleField, dateField, referenceField }
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <InputField
      :id="inputFields.noteField.id"
      :aria-label="inputFields.noteField.label"
      :label="inputFields.noteField.label"
    >
      <TextInput
        :id="inputFields.noteField.id"
        :aria-label="inputFields.noteField.label"
        :model-value="inputFields.noteField.modelValue"
        @update:model-value="inputFields.noteField.updateModelValue"
      />
    </InputField>

    <InputField
      :id="inputFields.descriptionOrArticleField.id"
      :aria-label="inputFields.descriptionOrArticleField.label"
      :label="inputFields.descriptionOrArticleField.label"
    >
      <TextInput
        :id="inputFields.descriptionOrArticleField.id"
        :aria-label="inputFields.descriptionOrArticleField.label"
        :model-value="inputFields.descriptionOrArticleField.modelValue"
        @update:model-value="
          inputFields.descriptionOrArticleField.updateModelValue
        "
      />
    </InputField>

    <InputField
      :id="inputFields.dateField.id"
      :aria-label="inputFields.dateField.label"
      :label="inputFields.dateField.label"
    >
      <DateInput
        :id="inputFields.dateField.id"
        :aria-label="inputFields.dateField.label"
        :model-value="inputFields.dateField.modelValue"
        @update:model-value="inputFields.dateField.updateModelValue"
      />
    </InputField>

    <InputField
      :id="inputFields.referenceField.id"
      :aria-label="inputFields.referenceField.label"
      :label="inputFields.referenceField.label"
    >
      <TextInput
        v-if="inputFields.referenceField.multi === false"
        :id="inputFields.referenceField.id"
        :aria-label="inputFields.referenceField.label"
        :model-value="inputFields.referenceField.modelValue?.toString()"
        @update:model-value="inputFields.referenceField.updateModelValue"
      />

      <ChipsInput
        v-else-if="inputFields.referenceField.multi === true"
        :id="inputFields.referenceField.id"
        :aria-label="inputFields.referenceField.label"
        :model-value="(inputFields.referenceField.modelValue as string[])"
        @update:model-value="inputFields.referenceField.updateModelValue"
      />
    </InputField>
  </div>
</template>
