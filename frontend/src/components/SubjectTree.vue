<script lang="ts" setup>
import { onMounted, ref } from "vue"
import SubjectNode from "@/components/SubjectNode.vue"
import SubjectTree from "@/domain/SubjectTree"
import SubjectsService from "@/services/subjectsService"

const tree = ref<SubjectTree>()

function fetchTree() {
  SubjectsService.getAllNodes().then((response) => {
    if (!response.data) return
    tree.value = new SubjectTree(response.data)
  })
}

function handleNodeClick(nodeId: string) {
  tree.value?.toggleNode(nodeId)
}

onMounted(fetchTree)
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <SubjectNode
    v-for="node in tree?.getOrderedNodes()"
    :key="node.id"
    :node="node"
    @node:toggle="handleNodeClick"
  />
</template>
