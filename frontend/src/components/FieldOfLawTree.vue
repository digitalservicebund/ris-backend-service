<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { NodeHelper, NodeHelperInterface } from "@/components/fieldOfLawNode"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { buildRoot, FieldOfLaw } from "@/domain/fieldOfLaw"

const props = defineProps<{
  modelValue: FieldOfLaw[]
  selectedNode?: FieldOfLaw
  searchResults?: FieldOfLaw[]
  showNorms: boolean
}>()

const emit = defineEmits<{
  "node:select": [node: FieldOfLaw]
  "node:unselect": [node: FieldOfLaw]
  "selected-node:reset": []
  "linked-field:select": [node: FieldOfLaw]
  "toggle-show-norms": []
}>()

const root = ref(buildRoot())
const nodeHelper = ref<NodeHelperInterface>(new NodeHelper())
const expandedNodes = ref<FieldOfLaw[]>([])
const showNormsModelValue = computed({
  get: () => props.showNorms,
  set: () => emit("toggle-show-norms"),
})

async function expandSelectedNode(node: FieldOfLaw) {
  const itemsToReturn = new Map<string, FieldOfLaw>()
  if (props.selectedNode) {
    itemsToReturn.set(node.identifier, node)
    const response = await nodeHelper.value.getAncestors(
      props.selectedNode.identifier,
    )
    for (const node of response) {
      itemsToReturn.set(node.identifier, node)
    }
    itemsToReturn.set(root.value.identifier, root.value)
  }
  expandedNodes.value = Array.from(itemsToReturn.values())
}

async function expandSelectedNodes(node: FieldOfLaw) {
  const itemsToReturn = new Map<string, FieldOfLaw>()

  if (node.identifier == "root") {
    itemsToReturn.set(node.identifier, node)
    for (const selected of props.modelValue) {
      const response = await nodeHelper.value.getAncestors(selected.identifier)
      for (const node of response) {
        itemsToReturn.set(node.identifier, node)
      }
    }
    expandedNodes.value = Array.from(addExpandedNodes(itemsToReturn).values())
  } else {
    expandedNodes.value.push(node)
  }
}

function collapseNode(collapsedNode: FieldOfLaw) {
  expandedNodes.value = expandedNodes.value.filter(
    (node) => node.identifier !== collapsedNode.identifier,
  )
}

function addExpandedNodes(
  map: Map<string, FieldOfLaw>,
): Map<string, FieldOfLaw> {
  expandedNodes.value.forEach((node) => {
    map.set(node.identifier, node)
  })
  return map
}

watch(
  () => props.selectedNode,
  async (newSelectedNode, oldSelectedNode) => {
    if (newSelectedNode !== oldSelectedNode) {
      if (props.selectedNode) await expandSelectedNode(props.selectedNode)
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-1 flex-col bg-blue-200 p-16">
    <div class="flex w-full flex-row justify-between">
      <div class="flex"><p class="ds-label-01-reg">Sachgebietsbaum</p></div>
      <div class="flex">
        <InputField
          id="showNorms"
          aria-label="Normen anzeigen"
          label="Mit Normen"
          label-class="ds-label-02-reg"
          :label-position="LabelPosition.RIGHT"
        >
          <CheckboxInput
            id="showNorms"
            v-model="showNormsModelValue"
            class="ds-checkbox-mini bg-white"
            size="small"
          />
        </InputField>
      </div>
    </div>

    <FieldOfLawNodeComponent
      :key="root.identifier"
      :expand-values="expandedNodes"
      is-root
      :model-value="modelValue"
      :node="root"
      :node-helper="nodeHelper"
      :search-results="searchResults"
      :selected-node="selectedNode"
      :show-norms="showNorms"
      @linked-field:select="emit('linked-field:select', $event)"
      @node:collapse="collapseNode"
      @node:expand="expandSelectedNodes"
      @node:select="emit('node:select', $event)"
      @node:unselect="emit('node:unselect', $event)"
      @selected-node:reset="emit('selected-node:reset')"
    />
  </div>
</template>
