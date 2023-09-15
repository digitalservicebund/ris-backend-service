<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed } from "vue"
import DocumentOtherInputGroup from "@/components/documentStatus/DocumentOtherInputGroup.vue"
import DocumentStatusInputGroup from "@/components/documentStatus/DocumentStatusInputGroup.vue"
import DocumentTextProofInputGroup from "@/components/documentStatus/DocumentTextProofInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<{
  modelValue: MetadataSections
}>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

const store = useLoadedNormStore()

const { loadedNorm } = storeToRefs(store)

/* -------------------------------------------------- *
 * Section type                                       *
 * -------------------------------------------------- */

const initialValue: MetadataSections = {
  DOCUMENT_STATUS: props.modelValue.DOCUMENT_STATUS,
  DOCUMENT_TEXT_PROOF: props.modelValue.DOCUMENT_TEXT_PROOF,
  DOCUMENT_OTHER: props.modelValue.DOCUMENT_OTHER,
}

const selectedChildSection = computed<
  | MetadataSectionName.DOCUMENT_STATUS
  | MetadataSectionName.DOCUMENT_TEXT_PROOF
  | MetadataSectionName.DOCUMENT_OTHER
>({
  get: () => {
    if (props.modelValue.DOCUMENT_STATUS?.[0]) {
      return MetadataSectionName.DOCUMENT_STATUS
    } else if (props.modelValue.DOCUMENT_TEXT_PROOF?.[0]) {
      return MetadataSectionName.DOCUMENT_TEXT_PROOF
    } else if (props.modelValue.DOCUMENT_OTHER?.[0]) {
      return MetadataSectionName.DOCUMENT_OTHER
    } else {
      return MetadataSectionName.DOCUMENT_STATUS
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [{}] })
  },
})

const disableDocumentTextProof = computed(() => {
  const hasTextProof =
    loadedNorm.value?.metadataSections?.DOCUMENT_STATUS_SECTION?.some(
      (entry) => entry.DOCUMENT_TEXT_PROOF,
    ) ?? false

  return (
    hasTextProof &&
    selectedChildSection.value !== MetadataSectionName.DOCUMENT_TEXT_PROOF
  )
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const documentStatusSection = computed({
  get: () => props.modelValue.DOCUMENT_STATUS?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DOCUMENT_STATUS = effectiveData

    const next: MetadataSections = { DOCUMENT_STATUS: effectiveData }
    emit("update:modelValue", next)
  },
})

const documentTextProofSection = computed({
  get: () => props.modelValue.DOCUMENT_TEXT_PROOF?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DOCUMENT_TEXT_PROOF = effectiveData

    const next: MetadataSections = { DOCUMENT_TEXT_PROOF: effectiveData }
    emit("update:modelValue", next)
  },
})

const documentOtherSection = computed({
  get: () => props.modelValue.DOCUMENT_OTHER?.[0] ?? {},
  set: (data?: Metadata) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DOCUMENT_OTHER = effectiveData

    const next: MetadataSections = { DOCUMENT_OTHER: effectiveData }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="mb-8 flex flex-row flex-wrap gap-24">
      <div>
        <InputField
          id="documentStatusSelection"
          v-slot="{ id }"
          label="Stand der dokumentarischen Bearbeitung"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSection"
            name="documentStatusSection"
            size="medium"
            :value="MetadataSectionName.DOCUMENT_STATUS"
          />
        </InputField>
      </div>

      <div>
        <InputField
          id="documentTextProofSelection"
          v-slot="{ id }"
          label="Textnachweis"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSection"
            :disabled="disableDocumentTextProof"
            name="documentStatusSection"
            size="medium"
            :value="MetadataSectionName.DOCUMENT_TEXT_PROOF"
          />
        </InputField>
      </div>

      <div>
        <InputField
          id="documentOtherSelection"
          v-slot="{ id }"
          label="Sonstiger Hinweis"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedChildSection"
            name="documentStatusSection"
            size="medium"
            :value="MetadataSectionName.DOCUMENT_OTHER"
          />
        </InputField>
      </div>
    </div>

    <DocumentStatusInputGroup
      v-if="selectedChildSection === MetadataSectionName.DOCUMENT_STATUS"
      v-model="documentStatusSection"
    />

    <DocumentTextProofInputGroup
      v-else-if="
        selectedChildSection === MetadataSectionName.DOCUMENT_TEXT_PROOF
      "
      v-model="documentTextProofSection"
    />

    <DocumentOtherInputGroup
      v-else-if="selectedChildSection === MetadataSectionName.DOCUMENT_OTHER"
      v-model="documentOtherSection"
    />
  </div>
</template>
