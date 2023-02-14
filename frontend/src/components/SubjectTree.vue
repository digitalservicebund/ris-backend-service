<script lang="ts" setup>
import { onMounted, ref, watch } from "vue"
import SubjectNodeComponent from "./SubjectNodeComponent.vue"
import SubjectTree, { SubjectNode } from "@/domain/SubjectTree"
import SubjectsService from "@/services/subjectsService"

const props = defineProps<{
  selectedSubjects: SubjectNode[]
  selectedNode: SubjectNode | undefined
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: SubjectNode): void
  (event: "delete-from-list", id: string): void
}>()

const tree = ref<SubjectTree>()

watch(
  () => props.selectedNode,
  () => {
    // console.log("change in selected node")
    fetchTree()
  }
)

function fetchTree() {
  SubjectsService.getRootNode().then((response) => {
    if (!response.data) return
    tree.value = new SubjectTree(response.data)

    if (props.selectedNode) {
      tree.value.root.children = []
      SubjectsService.getTreeForSubjectFieldNumber(props.selectedNode.id).then(
        (response) => {
          // console.log("loaded tree", response.data)
          if (!response.data || !tree.value) return

          expandAllChilds(response.data, 1)

          tree.value.root.children?.push(response.data)
          tree.value.root.isExpanded = true
        }
      )
    }
  })
}

function expandAllChilds(node: SubjectNode, depth: number) {
  // console.log("expand", node.id)
  // node.id = node.subjectFieldNumber
  // node.stext = node.subjectFieldText
  node.depth = depth
  node.isExpanded = true
  node.children?.forEach((child) => {
    expandAllChilds(child, depth + 1)
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
    :selected="props.selectedSubjects.some(({ id }) => id === node.id)"
    @node:add="handleAdd"
    @node:delete="handleDelete"
    @node:toggle="handleNodeClick"
  />
</template>
