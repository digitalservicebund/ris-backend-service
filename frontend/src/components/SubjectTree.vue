<script lang="ts" setup>
import { ref } from "vue"
import { SubjectNode } from "@/domain/SubjectTree"
import SubjectsService from "@/services/subjectsService"

const nodes = ref<SubjectNode[]>()

function expand() {
  SubjectsService.getAllNodes().then((response) => {
    nodes.value = response.data
  })
}

function buildNodeText(node: SubjectNode | undefined) {
  if (!node || node.depth === undefined) return ""
  return Array(node.depth + 1).join("-") + " " + node.id + ": " + node.stext
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <button class="align-middle pr-4 text-white" @click="expand">
    <span
      aria-label="Sachgebietsbaum aufklappen"
      class="bg-blue-800 material-icons rounded-full w-icon"
      >add</span
    >
  </button>
  Alle Sachgebiete anzeigen
  <div v-for="node in nodes" :key="node.id">
    {{ buildNodeText(node) }}
  </div>
</template>
