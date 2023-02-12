<script lang="ts" setup>
import { onMounted, ref } from "vue"
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
  console.log(nodeId)
}

onMounted(fetchTree)
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <div
    v-for="node in tree?.getOrderedNodes()"
    :key="node.id"
    :style="{ 'padding-left': `${node.depth * 40}px` }"
  >
    <button
      class="align-middle pr-4 text-blue-800"
      @click="handleNodeClick(node.id)"
    >
      <span
        aria-label="Sachgebietsbaum aufklappen"
        class="bg-blue-200 material-icons rounded-full w-icon"
        >add</span
      >
    </button>
    <span v-if="node.id !== 'root'" class="pl-6">{{ node.id }}</span>
    <span class="pl-6 text-blue-800 text-sm">{{ node.stext }}</span>
  </div>
</template>
