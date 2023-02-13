<script lang="ts" setup>
import { onMounted, ref } from "vue"
import SubjectNodeComponent from "./SubjectNodeComponent.vue"
import SubjectTree, { SubjectNode } from "@/domain/SubjectTree"
import SubjectsService from "@/services/subjectsService"

const props = defineProps<{
  selectedSubjects: SubjectNode[]
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: SubjectNode): void
  (event: "delete-from-list", id: string): void
}>()

const tree = ref<SubjectTree>()

function fetchTree() {
  SubjectsService.getRootNode().then((response) => {
    if (!response.data) return
    tree.value = new SubjectTree(response.data)
  })
}

function handleNodeClick(node: SubjectNode) {
  tree.value?.toggleNode(node)
  // console.log(toRaw(tree.value))
}
function handleAdd(node: SubjectNode) {
  emit("add-to-list", node)
}
function handleDelete(nodeId: string) {
  emit("delete-from-list", nodeId)
}

onMounted(fetchTree)
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <SubjectNodeComponent
    v-for="node in tree?.getOrderedNodes()"
    :key="node.id"
    :node="node"
    :selected="props.selectedSubjects.some(({ id }) => id == node.id)"
    @node:add="handleAdd"
    @node:delete="handleDelete"
    @node:toggle="handleNodeClick"
  />
</template>
