<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, ref, watch } from "vue"
import DocumentOtherInputGroup from "@/components/documentStatus/DocumentOtherInputGroup.vue"
import DocumentStatusInputGroup from "@/components/documentStatus/DocumentStatusInputGroup.vue"
import DocumentTextProofInputGroup from "@/components/documentStatus/DocumentTextProofInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const isDocumentTextProof = computed(() => {
  return (
    loadedNorm.value?.metadataSections?.DOCUMENT_STATUS_SECTION?.some(
      (entry) => entry.DOCUMENT_TEXT_PROOF,
    ) ?? false
  )
})

interface Props {
  modelValue: MetadataSections
}
type Emits = (event: "update:modelValue", value: MetadataSections) => void

type ChildSectionName =
  | MetadataSectionName.DOCUMENT_STATUS
  | MetadataSectionName.DOCUMENT_TEXT_PROOF
  | MetadataSectionName.DOCUMENT_OTHER

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DOCUMENT_STATUS,
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  },
)

watch(
  () => props.modelValue,
  (modelValue) => {
    if (modelValue.DOCUMENT_STATUS) {
      selectedChildSectionName.value = MetadataSectionName.DOCUMENT_STATUS
      childSection.value = modelValue.DOCUMENT_STATUS[0]
    } else if (modelValue.DOCUMENT_TEXT_PROOF) {
      selectedChildSectionName.value = MetadataSectionName.DOCUMENT_TEXT_PROOF
      childSection.value = modelValue.DOCUMENT_TEXT_PROOF[0]
    } else if (modelValue.DOCUMENT_OTHER) {
      selectedChildSectionName.value = MetadataSectionName.DOCUMENT_OTHER
      childSection.value = modelValue.DOCUMENT_OTHER[0]
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

watch(selectedChildSectionName, () => (childSection.value = {}))

const component = computed(() => {
  switch (selectedChildSectionName.value) {
    case MetadataSectionName.DOCUMENT_STATUS:
      return DocumentStatusInputGroup
    case MetadataSectionName.DOCUMENT_TEXT_PROOF:
      return DocumentTextProofInputGroup
    case MetadataSectionName.DOCUMENT_OTHER:
      return DocumentOtherInputGroup
    default:
      throw new Error(
        `Unknown document status child section: "${selectedChildSectionName.value}"`,
      )
  }
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
            v-model="selectedChildSectionName"
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
            v-model="selectedChildSectionName"
            :disabled="
              isDocumentTextProof &&
              !(
                selectedChildSectionName ===
                MetadataSectionName.DOCUMENT_TEXT_PROOF
              )
            "
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
            v-model="selectedChildSectionName"
            name="documentStatusSection"
            size="medium"
            :value="MetadataSectionName.DOCUMENT_OTHER"
          />
        </InputField>
      </div>
    </div>

    <component :is="component" v-model="childSection" />
  </div>
</template>
