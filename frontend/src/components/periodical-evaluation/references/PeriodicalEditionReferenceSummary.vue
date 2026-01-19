<script lang="ts" setup>
import Button from "primevue/button"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import Reference from "@/domain/reference"
import AddIcon from "~icons/ic/outline-library-add"

const props = defineProps<{
  data: Reference
}>()

const emit = defineEmits<{
  addNewEntry: [value: Reference]
}>()

async function generateNewEntryFromExisting() {
  emit(
    "addNewEntry",
    new Reference({
      documentationUnit: props.data.documentationUnit,
      primaryReference: props.data.primaryReference,
      legalPeriodical: props.data.legalPeriodical,
    }),
  )
}
</script>

<template>
  <div class="flex w-full justify-between" data-testid="reference-list-summary">
    <div v-if="props.data.documentationUnit?.documentNumber">
      <div class="ris-label1-bold" data-testid="citation-summary">
        {{ props.data.renderSummary }}
      </div>
      <DecisionSummary
        :display-mode="DisplayMode.SIDEPANEL"
        :document-number="props.data.documentationUnit.documentNumber"
        :link-clickable="props.data.documentationUnit.hasPreviewAccess"
        :status="props.data.documentationUnit.status"
        :summary="props.data.documentationUnit.renderSummary"
      ></DecisionSummary>
    </div>

    <Button
      v-tooltip.bottom="'Weitere Fundstelle zu dieser Entscheidung'"
      aria-label="Weitere Fundstelle zu dieser Entscheidung"
      data-testid="import-categories"
      size="small"
      text
      @click="generateNewEntryFromExisting"
      ><template #icon> <AddIcon /> </template
    ></Button>
  </div>
</template>
