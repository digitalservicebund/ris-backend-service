<script lang="ts" setup>
import { computed, h } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"

const props = defineProps<{
  modelValue: NormReference[] | undefined
}>()

const emit = defineEmits<{
  (event: "update:modelValue", value?: NormReference[]): void
}>()

const norms = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const defaultValue = {}

function decisionSummarizer(normEntry: NormReference) {
  return h("div", { class: ["link-02-reg"] }, normEntry.renderDecision)
}

const NormsSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <ExpandableDataSet
    id="normReferences"
    as-column
    :data-set="norms"
    :summary-component="NormsSummary"
    title="Normen"
  >
    <EditableList
      v-model="norms"
      :default-value="defaultValue"
      :edit-component="NormReferenceInput"
      :summary-component="NormsSummary"
    />
  </ExpandableDataSet>
</template>
