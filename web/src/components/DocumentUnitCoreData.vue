<script lang="ts" setup>
import { computed } from "vue"
import { CoreData } from "../domain/documentUnit"
import InputGroup from "./InputGroup.vue"
import SaveDocumentUnitButton from "./SaveDocumentUnitButton.vue"
import { coreDataFields } from "@/domain"

interface Props {
  modelValue?: CoreData
  updateStatus: number
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
</script>

<template>
  <div v-if="!modelValue">Loading...</div>

  <div v-else class="mb-[4rem]">
    <h1 class="heading-02-regular mb-[1rem]">Stammdaten</h1>

    <InputGroup v-model="values" :column-count="2" :fields="coreDataFields" />

    <div class="mt-4">* Pflichtfelder zum Veröffentlichen</div>

    <SaveDocumentUnitButton
      aria-label="Stammdaten Speichern Button"
      class="mt-8"
      :update-status="updateStatus"
      @update-document-unit="emit('updateDocumentUnit')"
    />
  </div>
</template>
