<script lang="ts" setup>
import { h, ref, watch } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FieldOfLawDirectInputSearch from "@/components/FieldOfLawDirectInputSearch.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

const props = defineProps<{
  modelValue: FieldOfLaw[] | undefined
}>()

const emit = defineEmits<{ "update:modelValue": [value?: FieldOfLaw[]] }>()

const showNorms = ref(false)
const localModelValue = ref<FieldOfLaw[]>(props.modelValue ?? [])
const selectedNode = ref<FieldOfLaw | undefined>(undefined)

watch(
  props,
  () => {
    localModelValue.value = props.modelValue ?? []
  },
  { immediate: true },
)

watch(localModelValue, () => {
  emit("update:modelValue", localModelValue.value)
})

const addFeldOfLaw = (fieldOfLaw: FieldOfLaw) => {
  if (
    !localModelValue.value?.find(
      (entry) => entry.identifier === fieldOfLaw.identifier,
    )
  ) {
    localModelValue.value?.push(fieldOfLaw)
  }
}

const removeFieldOfLaw = (fieldOfLaw: FieldOfLaw) => {
  localModelValue.value =
    localModelValue.value?.filter(
      (entry) => entry.identifier !== fieldOfLaw.identifier,
    ) ?? []
}

function setSelectedNode(node: FieldOfLaw) {
  selectedNode.value = node
  console.log("updated")
}

function removeSelectedNode() {
  selectedNode.value = undefined
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function selectedFieldsOfLawSummarizer(dataEntry: any) {
  return h("div", [
    h(
      "span",
      {
        class: "text-blue-800",
      },
      dataEntry.identifier,
    ),
    ", " + dataEntry.text,
  ])
}

const SelectedFieldsOfLawSummary = withSummarizer(selectedFieldsOfLawSummarizer)
</script>

<template>
  <ExpandableDataSet
    :data-set="localModelValue"
    :summary-component="SelectedFieldsOfLawSummary"
    title="Sachgebiete"
  >
    <div class="w-full">
      <div class="flex flex-row">
        <div class="flex flex-1 flex-col bg-white p-32">
          <FieldOfLawSearch
            @do-show-norms="showNorms = true"
            @linked-field:select="setSelectedNode"
            @node:select="setSelectedNode"
            @node:unselect="removeSelectedNode"
          />
        </div>
        <div class="flex-1 bg-white p-32">
          <FieldOfLawTree
            v-model="localModelValue"
            :selected-node="selectedNode"
            :show-norms="showNorms"
            @linked-field:select="setSelectedNode"
            @node:select="addFeldOfLaw"
            @node:unselect="removeFieldOfLaw"
            @toggle-show-norms="showNorms = !showNorms"
          ></FieldOfLawTree>
        </div>
      </div>
      <hr class="w-full border-blue-700" />
      <div class="bg-white p-20">
        <h1 class="ds-heading-03-reg pb-8">Ausgewählte Sachgebiete</h1>
        <FieldOfLawDirectInputSearch @add-to-list="addFeldOfLaw" />
        <FieldOfLawSelectionList
          v-model="localModelValue"
          @node:remove="removeFieldOfLaw"
          @node:select="setSelectedNode"
        ></FieldOfLawSelectionList>
      </div>
    </div>
  </ExpandableDataSet>
</template>
