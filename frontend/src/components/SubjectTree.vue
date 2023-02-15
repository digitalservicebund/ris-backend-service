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
  (event: "delete-from-list", subjectFieldNumber: string): void
  (event: "reset-selected-node"): void
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

function handleAdd(node: SubjectNode) {
  emit("add-to-list", node)
}

function handleDelete(subjectFieldNumber: string) {
  emit("delete-from-list", subjectFieldNumber)
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <SubjectNodeComponent
    v-for="node in tree?.getOrderedNodes()"
    :key="node.subjectFieldNumber"
    :node="node"
    :selected="
      props.selectedSubjects.some(
        ({ subjectFieldNumber }) =>
          subjectFieldNumber === node.subjectFieldNumber
      )
    "
    @node:add="handleAdd"
    @node:delete="handleDelete"
    @node:toggle="handleNodeClick"
  />
</template>
