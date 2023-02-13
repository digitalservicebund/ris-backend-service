<script lang="ts" setup>
import { ref } from "vue"
import SubjectSelectionList from "./SubjectSelectionList.vue"
import SubjectTree from "./SubjectTree.vue"
import { SubjectNode } from "@/domain/SubjectTree"

const selectedSubjects = ref<SubjectNode[]>([])

const getSubjectIndex = (subjParam: string) =>
  selectedSubjects.value.findIndex(({ id }) => id == subjParam)

function handleAddToList(node: SubjectNode) {
  if (getSubjectIndex(node.id) == -1) selectedSubjects.value.push(node)
}

function handleDeleteIdFromList(id: string) {
  if (getSubjectIndex(id) != -1) handleRemoveFromList(getSubjectIndex(id))
}

function handleRemoveFromList(index: number) {
  selectedSubjects.value.splice(index, 1)
}
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-02-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <SubjectSelectionList
          :selected-subjects="selectedSubjects"
          @remove-from-list="handleRemoveFromList($event)"
        ></SubjectSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <SubjectTree
          :selected-subjects="selectedSubjects"
          @add-to-list="handleAddToList($event)"
          @delete-from-list="handleDeleteIdFromList($event)"
        ></SubjectTree>
      </div>
    </div>
  </div>
</template>
