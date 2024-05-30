<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { NodeHelper, NodeHelperInterface } from "@/components/fieldOfLawNode"
import FlexContainer from "@/components/FlexContainer.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { buildRoot, FieldOfLaw } from "@/domain/fieldOfLaw"

const props = defineProps<{
  modelValue: FieldOfLaw[]
  selectedNode?: FieldOfLaw
  showNorms: boolean
}>()

const emit = defineEmits<{
  "node:select": [node: FieldOfLaw]
  "node:unselect": [node: FieldOfLaw]
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
  }
  expandedNodes.value = Array.from(itemsToReturn.values())
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
  <FlexContainer flex-direction="flex-col" justify-content="justify-between">
    <h1 class="ds-heading-03-reg pb-10">Sachgebietsbaum</h1>
    <div class="my-14">
      <InputField
        id="showNorms"
        aria-label="Normen anzeigen"
        label="Normen anzeigen"
        label-class="ds-label-01-reg"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          id="showNorms"
          v-model="showNormsModelValue"
          size="small"
        />
      </InputField>
    </div>
  </FlexContainer>
  <FieldOfLawNodeComponent
    :key="root.identifier"
    :expand-values="expandedNodes"
    is-root
    :model-value="modelValue"
    :node="root"
    :node-helper="nodeHelper"
    :selected-node="selectedNode"
    :show-norms="showNorms"
    @linked-field:select="emit('linked-field:select', $event)"
    @node:expanded="expandSelectedNodes"
    @node:select="emit('node:select', $event)"
    @node:unselect="emit('node:unselect', $event)"
  />
</template>
