<script lang="ts" setup>
import DefaultSummary from "@/components/DefaultSummary.vue"
import EditableList from "@/components/EditableList.vue"
import ParticipatingJudgesInput from "@/components/ParticipatingJudgesInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
</script>

<template>
  <div id="participatingJudges" class="ds-label-02-reg mb-16">{{ label }}</div>
  <div
    aria-label="Mitwirkende Richter"
    class="border-b-1 border-blue-300"
    data-testid="Mitwirkende Richter"
  >
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="store.documentUnit!.longTexts.participatingJudges"
          :edit-component="ParticipatingJudgesInput"
          :summary-component="DefaultSummary"
        />
      </div>
    </div>
  </div>
</template>
