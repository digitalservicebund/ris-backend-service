<script lang="ts" setup>
import { ref, watch } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import FieldOfLawTree, {
  buildRoot,
  FieldOfLawNode,
} from "@/domain/fieldOfLawTree"
import FieldOfLawService from "@/services/fieldOfLawService"

const props = defineProps<{
  selectedSubjects: FieldOfLawNode[]
  clickedSubjectFieldNumber: string
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: FieldOfLawNode): void
  (event: "remove-from-list", subjectFieldNumber: string): void
  (event: "reset-clicked-node"): void
  (event: "linkedField:clicked", subjectFieldNumber: string): void
}>()

const tree = ref<FieldOfLawTree>(new FieldOfLawTree(buildRoot()))

watch(
  () => props.clickedSubjectFieldNumber,
  () => {
    buildDirectPathTree()
  }
)

function buildDirectPathTree() {
  if (!props.clickedSubjectFieldNumber) return
  FieldOfLawService.getTreeForNumber(props.clickedSubjectFieldNumber).then(
    (response) => {
      // console.log("loaded tree", response.data)
      if (!response.data) return

      tree.value = new FieldOfLawTree(buildRoot([response.data]))
      tree.value.expandAll(true)
    }
  )
  emit("reset-clicked-node")
}

function handleNodeClick(node: FieldOfLawNode) {
  tree.value.toggleNode(node)
}

function handleSelect(node: FieldOfLawNode) {
  emit("add-to-list", node)
}

function handleUnselect(subjectFieldNumber: string) {
  emit("remove-from-list", subjectFieldNumber)
}

function handleLinkedFieldClicked(subjectFieldNumber: string) {
  emit("linkedField:clicked", subjectFieldNumber)
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <FieldOfLawNodeComponent
    v-for="node in tree?.getNodesOrderedByDepthFirstSearch()"
    :key="node.subjectFieldNumber"
    :node="node"
    :selected="
      props.selectedSubjects.some(
        ({ subjectFieldNumber }) =>
          subjectFieldNumber === node.subjectFieldNumber
      )
    "
    @linked-field:clicked="handleLinkedFieldClicked"
    @node:select="handleSelect"
    @node:toggle="handleNodeClick"
    @node:unselect="handleUnselect"
  />
</template>
