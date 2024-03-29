<script lang="ts" setup>
import { computed, h } from "vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  modelValue: NormReference[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: NormReference[]]
}>()

const norms = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const defaultValue = new NormReference()

function decisionSummarizer(normEntry: NormReference) {
  return h("div", [
    normEntry.hasMissingRequiredFields
      ? h("div", { class: ["flex flex-row items-center"] }, [
          h(h(IconErrorOutline), {
            "aria-label": "Fehlerhafte Eingabe",
            class: ["mr-8 text-red-800"],
          }),
          h(
            "div",
            { class: ["ds-label-02-bold text-red-800"] },
            normEntry.renderDecision,
          ),
        ])
      : h("div", { class: ["link-02-reg"] }, normEntry.renderDecision),
  ])
}

const NormsSummary = withSummarizer(decisionSummarizer)
</script>
<template>
  <div aria-label="Norm" class="bg-white p-32">
    <h2 class="ds-heading-03-reg mb-24">Normen</h2>
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
