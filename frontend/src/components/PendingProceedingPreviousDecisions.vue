<script lang="ts" setup>
import { computed } from "vue"
import PreviousDecisionInputGroup from "./PreviousDecisionInputGroup.vue"
import DocumentationUnitSummary from "@/components/DocumentationUnitSummary.vue"
import EditableList from "@/components/EditableList.vue"
import PreviousDecision from "@/domain/previousDecision"
import { usePendingProceedingStore } from "@/stores/pendingProceedingStore"

const store = usePendingProceedingStore()

const previousDecisions = computed({
  get: () => store.pendingProceeding!.previousDecisions as PreviousDecision[],
  set: (newValues) => {
    store.pendingProceeding!.previousDecisions = newValues
  },
})
</script>

<template>
  <div>
    <div aria-label="Vorgehende Entscheidung">
      <h2 class="ris-label1-bold mb-16">Vorgehende Entscheidungen</h2>
      <div class="flex flex-row">
        <div class="flex-1">
          <EditableList
            v-model="previousDecisions"
            :create-entry="() => new PreviousDecision()"
            :edit-component="PreviousDecisionInputGroup"
            :summary-component="DocumentationUnitSummary"
          />
        </div>
      </div>
    </div>
  </div>
</template>
