<script setup lang="ts">
import { computed } from "vue"
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
import DocumentUnit from "@/domain/documentUnit"
import { NormReference } from "@/domain/normReference"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()

const emit = defineEmits<{
  (e: "updateValue", updatedValue: NormReference[]): Promise<void>
  (e: "updateDocumentUnit"): Promise<void>
}>()

const norms = computed({
  get: () => {
    return props.documentUnit?.contentRelatedIndexing?.norms
      ? props.documentUnit?.contentRelatedIndexing?.norms
      : []
  },
  set: (value) => {
    emit("updateValue", value)
  },
})
</script>

<template>
  <div class="mb-[4rem]">
    <h1 class="heading-02-regular mb-[1rem]">Inhaltliche Erschließung</h1>
    <KeyWords :document-unit-uuid="props.documentUnit.uuid" />
    <FieldOfLawMain :document-unit-uuid="props.documentUnit.uuid" />
    <Norms :norms="norms" @update-value="emit('updateValue', norms)" />
    <!-- Aktivzitierung -->
  </div>
</template>
