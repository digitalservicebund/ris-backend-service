<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitDecisionSummary from "@/components/DocumentUnitDecisionSummary.vue"
import EditableList from "@/components/EditableList.vue"
import ParticipatingJudgesInput from "@/components/ParticipatingJudgesInput.vue"
import ParticipatingJudge from "@/domain/participatingJudge"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()

const participatingJudges = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.participatingJudges,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.participatingJudges = newValues
  },
})

const defaultValue = new ParticipatingJudge()
</script>

<template>
  <div class="ds-label-02-reg mb-16">{{ label }}</div>
  <div aria-label="Mitwirkende Richter" class="border-b-1 border-blue-300">
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="participatingJudges"
          :default-value="defaultValue"
          :edit-component="ParticipatingJudgesInput"
          :summary-component="DocumentUnitDecisionSummary"
        />
      </div>
    </div>
  </div>
</template>
