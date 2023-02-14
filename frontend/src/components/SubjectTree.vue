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
  (event: "delete-from-list", id: string): void
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
    tree.value.expandAll()
  })
}

function handleNodeClick(node: SubjectNode) {
  tree.value.toggleNode(node)
  // console.log(toRaw(tree.value))
}
function handleAdd(node: SubjectNode) {
  emit("add-to-list", node)
}
function handleDelete(nodeId: string) {
  emit("delete-from-list", nodeId)
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
