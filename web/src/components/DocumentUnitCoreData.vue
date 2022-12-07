<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import { CoreData } from "../domain/documentUnit"
import InputGroup from "./InputGroup.vue"
import SaveDocumentUnitButton from "./SaveDocumentUnitButton.vue"
import {
  courtFields,
  coreDataFields,
  prefilledDataFields,
  moreCategories,
  ValidationError,
} from "@/domain"

interface Props {
  modelValue?: CoreData
  updateStatus: number
  validationErrors?: ValidationError[]
}

interface Emits {
  (event: "updateDocumentUnit"): void
  (event: "update:modelValue", value: CoreData): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const values = computed({
  get: () => props.modelValue ?? {},
  set: (newValues) => emit("update:modelValue", newValues),
})

const containerWidth = ref()
const columnCount = computed(() => (containerWidth.value < 600 ? 1 : 2))

onMounted(() => {
  const editorContainer = document.querySelector(".core-data")
  if (editorContainer != null) resizeObserver.observe(editorContainer)
})

const resizeObserver = new ResizeObserver((entries) => {
  for (const entry of entries) {
    containerWidth.value = entry.contentRect.width
  }
})
</script>

<template>
  <div v-if="!modelValue">Loading...</div>

  <div v-else class="mb-[4rem]">
    <h1 class="core-data heading-02-regular mb-[1rem]">Stammdaten</h1>

    <InputGroup
      v-model="values"
      :column-count="1"
      :fields="courtFields"
      :validation-errors="props.validationErrors"
    />
    <InputGroup
      v-model="values"
      :column-count="columnCount"
      :fields="coreDataFields"
      :validation-errors="props.validationErrors"
    />
    <InputGroup
      v-model="values"
      :column-count="columnCount"
      :fields="prefilledDataFields"
      :validation-errors="props.validationErrors"
    />
    <InputGroup
      v-model="values"
      :column-count="columnCount"
      :fields="moreCategories"
      :validation-errors="props.validationErrors"
    />
    <div class="mt-4">* Pflichtfelder zum Veröffentlichen</div>

    <SaveDocumentUnitButton
      aria-label="Stammdaten Speichern Button"
      class="mt-8"
      :update-status="updateStatus"
      @update-document-unit="emit('updateDocumentUnit')"
    />
  </div>
</template>
