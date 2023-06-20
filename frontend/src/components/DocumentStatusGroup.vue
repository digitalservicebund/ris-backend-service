<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, ref, watch } from "vue"
import DocumentOtherInputGroup from "@/components/DocumentOtherInputGroup.vue"
import DocumentStatusInputGroup from "@/components/DocumentStatusInputGroup.vue"
import DocumentTextProofInputGroup from "@/components/DocumentTextProofInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const isDocumentTextProof = computed(() => {
  return (
    loadedNorm.value?.metadataSections?.DOCUMENT_STATUS_SECTION?.some(
      (entry) => entry.DOCUMENT_TEXT_PROOF
    ) ?? false
  )
})

interface Props {
  modelValue: MetadataSections
}

interface Emits {
  (event: "update:modelValue", value: MetadataSections): void
}

type ChildSectionName =
  | MetadataSectionName.DOCUMENT_STATUS
  | MetadataSectionName.DOCUMENT_TEXT_PROOF
  | MetadataSectionName.DOCUMENT_OTHER

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DOCUMENT_STATUS
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  }
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
  }
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
        `Unknown document status child section: "${selectedChildSectionName.value}"`
      )
  }
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="flex flex-wrap gap-176">
      <div class="flex gap-24 mb-24">
        <label class="form-control">
          <input
            id="documentStatusSelection"
            v-model="selectedChildSectionName"
            aria-label="Stand der dokumentarischen Bearbeitung"
            name="DocumentStatusSection"
            type="radio"
            :value="MetadataSectionName.DOCUMENT_STATUS"
          />
          Stand der dokumentarischen Bearbeitung
        </label>
        <label class="form-control">
          <input
            id="documentTextProofSelection"
            v-model="selectedChildSectionName"
            aria-label="Textnachweis"
            :disabled="
              isDocumentTextProof &&
              !(
                selectedChildSectionName ===
                MetadataSectionName.DOCUMENT_TEXT_PROOF
              )
            "
            name="DocumentStatusSection"
            type="radio"
            :value="MetadataSectionName.DOCUMENT_TEXT_PROOF"
          />
          <span>Textnachweis</span>
        </label>
        <label class="form-control">
          <input
            id="documentOtherSelection"
            v-model="selectedChildSectionName"
            aria-label="Sonstiger Hinweis"
            name="DocumentStatusSection"
            type="radio"
            :value="MetadataSectionName.DOCUMENT_OTHER"
          />
          Sonstiger Hinweis
        </label>
      </div>
    </div>
    <component :is="component" v-model="childSection" />
  </div>
</template>

<style lang="scss" scoped>
.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]::before {
  width: 0.9em;
  height: 0.9em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:hover,
input[type="radio"]:focus {
  border: 4px solid #004b76;
  outline: none;
}

input[type="radio"]:checked::before {
  transform: scale(1);
}

input[type="radio"]:disabled {
  color: #717a88;
}

input[type="radio"]:disabled + span {
  color: #717a88;
}

input[type="radio"]:disabled:hover {
  border-width: 0.15em;
  border-color: #717a88;
  outline: none;
}
</style>
