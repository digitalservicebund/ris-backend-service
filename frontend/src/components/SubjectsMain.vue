<script lang="ts" setup>
import { ref } from "vue"
import SubjectSelectionList from "./SubjectSelectionList.vue"
import SubjectTree from "./SubjectTree.vue"
import { SubjectNode } from "@/domain/SubjectTree"

const selectedSubjects = ref<SubjectNode[]>([])
const selectedNode = ref<SubjectNode>()

const getSubjectIndex = (_subjectFieldNumber: string) =>
  selectedSubjects.value.findIndex(
    ({ subjectFieldNumber }) => subjectFieldNumber == _subjectFieldNumber
  )

function handleAddToList(node: SubjectNode) {
  if (getSubjectIndex(node.subjectFieldNumber) == -1) {
    selectedSubjects.value.push(node)
  }
}

function handleDeleteIdFromList(subjectFieldNumber: string) {
  if (getSubjectIndex(subjectFieldNumber) != -1) {
    handleRemoveFromList(getSubjectIndex(subjectFieldNumber))
  }
}

function handleRemoveFromList(index: number) {
  selectedSubjects.value.splice(index, 1)
}

function handleNodeClicked(node: SubjectNode) {
  selectedNode.value = node
}

function handleResetSelectedNode() {
  selectedNode.value = undefined
}
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-02-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <SubjectSelectionList
          :selected-subjects="selectedSubjects"
          @node-clicked="handleNodeClicked"
          @remove-from-list="handleRemoveFromList"
        ></SubjectSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <SubjectTree
          :selected-node="selectedNode"
          :selected-subjects="selectedSubjects"
          @add-to-list="handleAddToList"
          @delete-from-list="handleDeleteIdFromList"
          @reset-selected-node="handleResetSelectedNode"
        ></SubjectTree>
      </div>
    </div>
  </div>
</template>
