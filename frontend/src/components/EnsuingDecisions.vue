<script lang="ts" setup>
import { computed } from "vue"
import EnsuingDecisionInputGroup from "./EnsuingDecisionInputGroup.vue"
import DocumentationUnitSummary from "@/components/DocumentationUnitSummary.vue"
import EditableList from "@/components/EditableList.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const ensuingDecisions = computed({
  get: () => store.documentUnit!.ensuingDecisions as EnsuingDecision[],
  set: (newValues) => {
    store.documentUnit!.ensuingDecisions = newValues
  },
})
</script>
<template>
  <div aria-label="Nachgehende Entscheidung">
    <h2 class="ris-label1-bold mb-16">Nachgehende Entscheidungen</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="ensuingDecisions"
          :create-entry="() => new EnsuingDecision()"
          :edit-component="EnsuingDecisionInputGroup"
          :summary-component="DocumentationUnitSummary"
        />
      </div>
    </div>
  </div>
</template>
