<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EditableList from "@/components/EditableList.vue"
import PendingProceedingInput from "@/components/PendingProceedingInput.vue"
import PendingProceedingSummary from "@/components/PendingProceedingSummary.vue"
import { Decision } from "@/domain/decision"
import RelatedPendingProceeding from "@/domain/pendingProceedingReference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
</script>

<template>
  <div id="relatedPendingProceedings" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.relatedPendingProceedings"
        :create-entry="() => new RelatedPendingProceeding()"
        :edit-component="PendingProceedingInput"
        :summary-component="PendingProceedingSummary"
      />
    </div>
  </div>
</template>
