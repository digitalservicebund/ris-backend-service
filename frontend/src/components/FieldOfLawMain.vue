<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLawTree"

const selectedSubjects = ref<FieldOfLawNode[]>([])
const clickedSubjectFieldNumber = ref("")

const getIndex = (_subjectFieldNumber: string) =>
  selectedSubjects.value.findIndex(
    ({ subjectFieldNumber }) => subjectFieldNumber == _subjectFieldNumber
  )

function handleAdd(node: FieldOfLawNode) {
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

function handleNodeClicked(node: FieldOfLawNode) {
  clickedSubjectFieldNumber.value = node.subjectFieldNumber
}

function handleResetClickedNode() {
  clickedSubjectFieldNumber.value = ""
}

function handleLinkedFieldClicked(subjectFieldNumber: string) {
  clickedSubjectFieldNumber.value = subjectFieldNumber
}
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-02-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <FieldOfLawSelectionList
          :selected-subjects="selectedSubjects"
          @node-clicked="handleNodeClicked"
          @remove-from-list="handleRemoveByIndex"
        ></FieldOfLawSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <FieldOfLawTree
          :clicked-subject-field-number="clickedSubjectFieldNumber"
          :selected-subjects="selectedSubjects"
          @add-to-list="handleAdd"
          @linked-field:clicked="handleLinkedFieldClicked"
          @remove-from-list="handleRemoveBySubjectFieldNumber"
          @reset-clicked-node="handleResetClickedNode"
        ></FieldOfLawTree>
      </div>
    </div>
  </div>
</template>
