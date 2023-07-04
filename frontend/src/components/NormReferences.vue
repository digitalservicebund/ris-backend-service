<script lang="ts" setup>
import { computed, h } from "vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableListCaselaw.vue"

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
  <div class="bg-white p-16">
    <h2 id="norms" class="label-02-bold mb-[1rem]">Normen</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="norms"
          :default-value="defaultValue"
          :edit-component="NormReferenceInput"
          :summary-component="NormsSummary"
        />
      </div>
    </div>
  </div>
</template>
