<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata, MetadataSectionName } from "@/domain/norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

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

const note = computed({
  get: () => props.modelValue.NOTE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.NOTE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const description = computed({
  get: () => props.modelValue.DESCRIPTION?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DESCRIPTION = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const article = computed({
  get: () => props.modelValue.ARTICLE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.ARTICLE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const date = computed({
  get: () => props.modelValue.DATE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DATE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const year = computed({
  get: () => props.modelValue.YEAR?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.YEAR = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const singleReference = computed({
  get: () => props.modelValue.REFERENCE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.REFERENCE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const multipleReferences = computed({
  get: () => props.modelValue.REFERENCE,
  set: (data?: string[]) => {
    const next = produce(props.modelValue, (draft) => {
      draft.REFERENCE = data ?? undefined
    })
    emit("update:modelValue", next)
  },
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

  const yearField =
    props.type === MetadataSectionName.STATUS
      ? {
          id: "statusYear",
          label: "Jahr",
          modelValue: year.value ?? "",
          updateModelValue: (value: string) => (year.value = value),
        }
      : {
          id: "reissueYear",
          label: "Jahr",
          modelValue: year.value ?? "",
          updateModelValue: (value: string) => (year.value = value),
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

  return {
    noteField,
    descriptionOrArticleField,
    dateField,
    yearField,
    referenceField,
  }
})

const dateEnabled = computed(() => !props.modelValue.YEAR?.[0])
const yearEnabled = computed(() => !props.modelValue.DATE?.[0])
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

    <div class="flex gap-24">
      <InputField
        :id="inputFields.dateField.id"
        v-slot="{ id, hasError, updateValidationError }"
        :aria-label="inputFields.dateField.label"
        class="md:w-auto"
        :label="inputFields.dateField.label"
      >
        <DateInput
          :id="id"
          v-model="inputFields.dateField.modelValue"
          :aria-label="`${inputFields.dateField.label}`"
          :disabled="!dateEnabled"
          :has-error="hasError"
          is-future-date
          @update:model-value="inputFields.dateField.updateModelValue"
          @update:validation-error="updateValidationError"
        />
      </InputField>
      <p class="my-auto">oder</p>
      <InputField
        :id="inputFields.yearField.id"
        v-slot="{ id, hasError, updateValidationError }"
        :aria-label="inputFields.yearField.label"
        class="md:w-auto"
        :label="inputFields.yearField.label"
      >
        <YearInput
          :id="id"
          v-model="inputFields.yearField.modelValue"
          :aria-label="inputFields.yearField.label"
          :disabled="!yearEnabled"
          :has-error="hasError"
          @update:model-value="inputFields.yearField.updateModelValue"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </div>

    <InputField
      :id="inputFields.referenceField.id"
      v-slot="{ id }"
      :aria-label="inputFields.referenceField.label"
      :label="inputFields.referenceField.label"
    >
      <TextInput
        v-if="!inputFields.referenceField.multi"
        :id="id"
        :aria-label="inputFields.referenceField.label"
        :model-value="inputFields.referenceField.modelValue?.toString()"
        @update:model-value="inputFields.referenceField.updateModelValue"
      />

      <ChipsInput
        v-else-if="inputFields.referenceField.multi"
        :id="id"
        :aria-label="inputFields.referenceField.label"
        :model-value="inputFields.referenceField.modelValue as string[]"
        @update:model-value="inputFields.referenceField.updateModelValue"
      />
    </InputField>
  </div>
</template>
