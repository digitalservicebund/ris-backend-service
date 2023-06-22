<script lang="ts" setup>
import { h, computed } from "vue"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import ActiveCitation from "@/domain/activeCitation"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableListCaselaw.vue"

const props = defineProps<{
  modelValue: ActiveCitation[] | undefined
}>()

const emit = defineEmits<{
  (event: "update:modelValue", value?: ActiveCitation[]): void
}>()

const activeCitations = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const defaultValue = {}

function decisionSummarizer(activeCitation: ActiveCitation) {
  return h("div", { class: ["link-02-reg"] }, activeCitation.renderDecision)
}

const CitationsSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div class="bg-white p-16">
    <h2 class="label-02-bold mb-[1rem]">Aktivzitierung</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="activeCitations"
          :default-value="defaultValue"
          :edit-component="ActiveCitationInput"
          :summary-component="CitationsSummary"
        />
      </div>
    </div>
  </div>
</template>
