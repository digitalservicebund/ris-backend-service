<script lang="ts" setup>
import { ref, watch } from "vue"
import SubjectNodeComponent from "./SubjectNodeComponent.vue"
import SubjectTree, { buildRoot, SubjectNode } from "@/domain/SubjectTree"
import SubjectsService from "@/services/subjectsService"

const props = defineProps<{
  selectedSubjects: SubjectNode[]
  selectedNode: SubjectNode | undefined
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: SubjectNode): void
  (event: "remove-from-list", subjectFieldNumber: string): void
  (event: "reset-selected-node"): void
  (event: "linkedField:clicked", subjectFieldNumber: string): void
}>()

const tree = ref<SubjectTree>(new SubjectTree(buildRoot()))

watch(
  () => props.selectedNode,
  () => {
    buildDirectPathTree()
  }
)

function buildDirectPathTree() {
  if (!props.selectedNode) return
  SubjectsService.getTreeForSubjectFieldNumber(
    props.selectedNode.subjectFieldNumber
  ).then((response) => {
    // console.log("loaded tree", response.data)
    if (!response.data) return
    tree.value = new SubjectTree(buildRoot([response.data]))
    tree.value.expandAll(true)
  })
  emit("reset-selected-node")
}

function handleNodeClick(node: SubjectNode) {
  tree.value.toggleNode(node)
}

function handleSelect(node: SubjectNode) {
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
  <SubjectNodeComponent
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
