<script lang="ts" setup>
import Checkbox from "primevue/checkbox"
import { computed, ref, watch } from "vue"
import FieldOfLawTreeNode from "./FieldOfLawTreeNode.vue"

import {
  NodeHelper,
  NodeHelperInterface,
} from "@/components/field-of-law/fieldOfLawNode"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { buildRoot, type FieldOfLaw } from "@/domain/fieldOfLaw"

const props = defineProps<{
  selectedNodes: FieldOfLaw[]
  nodeOfInterest?: FieldOfLaw
  searchResults?: FieldOfLaw[]
  showNorms: boolean
}>()

const emit = defineEmits<{
  "node:add": [node: FieldOfLaw]
  "node:remove": [node: FieldOfLaw]
  "node-of-interest:reset": []
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

async function expandNodeOfInterest(node: FieldOfLaw) {
  const mapOfTreeNodesToExpand = new Map<string, FieldOfLaw>()

  mapOfTreeNodesToExpand.set(root.value.identifier, root.value)

  const response = await nodeHelper.value.getAncestors(node.identifier)
  for (const ancestorNode of response) {
    mapOfTreeNodesToExpand.set(ancestorNode.identifier, ancestorNode)
  }

  expandedNodes.value = Array.from(mapOfTreeNodesToExpand.values())
}

async function expandSelectedNodesFromRoot() {
  const mapOfTreeNodesToExpand = new Map<string, FieldOfLaw>()

  mapOfTreeNodesToExpand.set(root.value.identifier, root.value)

  for (const selected of props.selectedNodes) {
    const response = await nodeHelper.value.getAncestors(selected.identifier)
    for (const node of response) {
      mapOfTreeNodesToExpand.set(node.identifier, node)
    }
  }
  expandedNodes.value = Array.from(
    addExpandedNodes(mapOfTreeNodesToExpand).values(),
  )
}

function expandNode(node: FieldOfLaw) {
  expandedNodes.value = [...expandedNodes.value, node]
}

function collapseNode(collapsedNode: FieldOfLaw) {
  expandedNodes.value = expandedNodes.value.filter(
    (node) => node.identifier !== collapsedNode.identifier,
  )
}

function addExpandedNodes(
  map: Map<string, FieldOfLaw>,
): Map<string, FieldOfLaw> {
  for (const node of expandedNodes.value) {
    map.set(node.identifier, node)
  }
  return map
}

function collapseTree() {
  expandedNodes.value = []
}

watch(
  () => props.nodeOfInterest,
  async (newValue, oldValue) => {
    if (newValue !== oldValue && props.nodeOfInterest) {
      await expandNodeOfInterest(props.nodeOfInterest)
    }
  },
  { immediate: true },
)

defineExpose({ collapseTree })
</script>

<template>
  <div class="flex flex-1 flex-col bg-blue-200 p-16">
    <div class="mb-20 flex w-full flex-row justify-between">
      <div class="flex">
        <p class="ris-label1-regular">Sachgebietsbaum</p>
      </div>
      <div class="flex">
        <InputField
          id="showNorms"
          aria-label="Normen anzeigen"
          data-testid="showNorms"
          label="Mit Normen"
          label-class="ris-label2-regular"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox v-model="showNormsModelValue" binary input-id="showNorms" />
        </InputField>
      </div>
    </div>

    <FieldOfLawTreeNode
      :key="root.identifier"
      :expanded-nodes="expandedNodes"
      is-root
      :node="root"
      :node-helper="nodeHelper"
      :node-of-interest="nodeOfInterest"
      :search-results="searchResults"
      :selected-nodes="selectedNodes"
      :show-norms="showNorms"
      @node-of-interest:reset="emit('node-of-interest:reset')"
      @node:add="emit('node:add', $event)"
      @node:collapse="collapseNode"
      @node:expand="expandNode"
      @node:expand-root="expandSelectedNodesFromRoot"
      @node:remove="emit('node:remove', $event)"
    />
  </div>
</template>
