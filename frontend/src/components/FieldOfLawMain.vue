<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const selectedNodes = ref<FieldOfLawNode[]>([])
const clickedIdentifier = ref("")

const getIndex = (_identifier: string) =>
  selectedNodes.value.findIndex(({ identifier }) => identifier == _identifier)

function handleAdd(node: FieldOfLawNode) {
  if (getIndex(node.identifier) == -1) {
    selectedNodes.value.push(node)
  }
}

function handleRemoveByIdentifier(identifier: string) {
  if (getIndex(identifier) != -1) {
    handleRemoveByIndex(getIndex(identifier))
  }
}

function handleRemoveByIndex(index: number) {
  selectedNodes.value.splice(index, 1)
}

function handleNodeClicked(node: FieldOfLawNode) {
  clickedIdentifier.value = node.identifier
}

function handleResetClickedNode() {
  clickedIdentifier.value = ""
}

function handleLinkedFieldClicked(identifier: string) {
  clickedIdentifier.value = identifier
}
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-03-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <FieldOfLawSelectionList
          :selected-nodes="selectedNodes"
          @node-clicked="handleNodeClicked"
          @remove-from-list="handleRemoveByIndex"
        ></FieldOfLawSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <FieldOfLawTree
          :clicked-identifier="clickedIdentifier"
          :selected-nodes="selectedNodes"
          @add-to-list="handleAdd"
          @linked-field:clicked="handleLinkedFieldClicked"
          @remove-from-list="handleRemoveByIdentifier"
          @reset-clicked-node="handleResetClickedNode"
        ></FieldOfLawTree>
      </div>
    </div>
  </div>
</template>
