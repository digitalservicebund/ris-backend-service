<script lang="ts" setup>
import { ref } from "vue"
import SubjectSelectionList from "./SubjectSelectionList.vue"
import SubjectTree from "./SubjectTree.vue"
import { SubjectNode } from "@/domain/SubjectTree"

const selectedSubjects = ref<SubjectNode[]>([])
const selectedNode = ref<SubjectNode>()

const getIndex = (_subjectFieldNumber: string) =>
  selectedSubjects.value.findIndex(
    ({ subjectFieldNumber }) => subjectFieldNumber == _subjectFieldNumber
  )

function handleAdd(node: SubjectNode) {
  if (getIndex(node.subjectFieldNumber) == -1) {
    selectedSubjects.value.push(node)
  }
}

function handleRemoveBySubjectFieldNumber(subjectFieldNumber: string) {
  if (getIndex(subjectFieldNumber) != -1) {
    handleRemoveByIndex(getIndex(subjectFieldNumber))
  }
}

function handleRemoveByIndex(index: number) {
  selectedSubjects.value.splice(index, 1)
}

function handleNodeClicked(node: SubjectNode) {
  selectedNode.value = node
}

function handleResetSelectedNode() {
  selectedNode.value = undefined
}

function handleLinkedFieldClicked(subjectFieldNumber: string) {
  console.log("SubjectsMain.handleLinkedFieldClicked()", subjectFieldNumber)
  // TODO
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
          @remove-from-list="handleRemoveByIndex"
        ></SubjectSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <SubjectTree
          :selected-node="selectedNode"
          :selected-subjects="selectedSubjects"
          @add-to-list="handleAdd"
          @linked-field:clicked="handleLinkedFieldClicked"
          @remove-from-list="handleRemoveBySubjectFieldNumber"
          @reset-selected-node="handleResetSelectedNode"
        ></SubjectTree>
      </div>
    </div>
  </div>
</template>
