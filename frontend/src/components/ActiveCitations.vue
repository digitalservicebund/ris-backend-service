<script lang="ts" setup>
import { h, computed } from "vue"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
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
  <ExpandableDataSet
    id="activeCitations"
    as-column
    :data-set="activeCitations"
    :summary-component="CitationsSummary"
    title="Aktivzitierung"
  >
    <EditableList
      v-model="activeCitations"
      :default-value="defaultValue"
      :edit-component="ActiveCitationInput"
      :summary-component="CitationsSummary"
    />
  </ExpandableDataSet>
</template>
