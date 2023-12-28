<script lang="ts" setup>
import { h, ref, watch } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FieldOfLawDirectInputSearch from "@/components/FieldOfLawDirectInputSearch.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"

const props = defineProps<{
  modelValue: FieldOfLawNode[] | undefined
}>()

const emit = defineEmits<{ "update:modelValue": [value?: FieldOfLawNode[]] }>()

const clickedIdentifier = ref("")
const showNorms = ref(false)
const localModelValue = ref<FieldOfLawNode[]>(props.modelValue ?? [])

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

const handleAdd = async (fieldOfLaw: FieldOfLawNode) => {
  if (
    !localModelValue.value?.find(
      (entry) => entry.identifier === fieldOfLaw.identifier,
    )
  ) {
    localModelValue.value?.push(fieldOfLaw)
  }
}

function handleNodeClicked(identifier: string) {
  clickedIdentifier.value = identifier
}

function handleResetClickedNode() {
  clickedIdentifier.value = ""
}

function handleLinkedFieldClicked(identifier: string) {
  clickedIdentifier.value = identifier
}

function handleIdentifierClickInSummary(identifier: string) {
  setTimeout(() => {
    clickedIdentifier.value = identifier
  }, 20)
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function selectedFieldsOfLawSummarizer(dataEntry: any) {
  return h("div", [
    h(
      "span",
      {
        class: "text-blue-800",
        onClick: () => handleIdentifierClickInSummary(dataEntry.identifier),
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
    as-column
    :data-set="localModelValue"
    :summary-component="SelectedFieldsOfLawSummary"
    title="Sachgebiete"
  >
    <div class="w-full">
      <div class="flex flex-row">
        <div class="flex flex-1 flex-col bg-white p-32">
          <FieldOfLawSearch
            @do-show-norms="showNorms = true"
            @node-clicked="handleNodeClicked"
          />
        </div>
        <div class="flex-1 bg-white p-20">
          <FieldOfLawTree
            v-model="localModelValue"
            :clicked-identifier="clickedIdentifier"
            :show-norms="showNorms"
            @linked-field:clicked="handleLinkedFieldClicked"
            @reset-clicked-node="handleResetClickedNode"
            @toggle-show-norms="showNorms = !showNorms"
          ></FieldOfLawTree>
        </div>
      </div>
      <hr class="w-full border-blue-700" />
      <div class="bg-white p-20">
        <h1 class="ds-heading-03-reg pb-8">Ausgewählte Sachgebiete</h1>
        <FieldOfLawDirectInputSearch @add-to-list="handleAdd" />
        <FieldOfLawSelectionList
          v-model="localModelValue"
          @linked-field:clicked="handleLinkedFieldClicked"
          @node-clicked="handleNodeClicked"
        ></FieldOfLawSelectionList>
      </div>
    </div>
  </ExpandableDataSet>
</template>
