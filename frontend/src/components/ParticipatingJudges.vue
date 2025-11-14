<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import DefaultSummary from "@/components/DefaultSummary.vue"
import EditableList from "@/components/EditableList.vue"
import ParticipatingJudgesInput from "@/components/ParticipatingJudgesInput.vue"
import { Decision } from "@/domain/decision"
import ParticipatingJudge from "@/domain/participatingJudge"
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
  <div id="participatingJudges" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div aria-label="Mitwirkende Richter" data-testid="Mitwirkende Richter">
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="decision!.longTexts.participatingJudges"
          :create-entry="() => new ParticipatingJudge()"
          :edit-component="ParticipatingJudgesInput"
          :summary-component="DefaultSummary"
        />
      </div>
    </div>
  </div>
</template>
