<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

const props = defineProps<{
  documentUnitUuid: string
}>()

const selectedFieldsOfLaw = ref<FieldOfLawNode[]>([])
const clickedIdentifier = ref("")

const response = await FieldOfLawService.getSelectedFieldsOfLaw(
  props.documentUnitUuid
)

if (response.data) {
  selectedFieldsOfLaw.value = response.data
}

const handleAdd = async (node: FieldOfLawNode) => {
  const response = await FieldOfLawService.addFieldOfLaw(
    props.documentUnitUuid,
    node.identifier
  )

  if (response.data) {
    selectedFieldsOfLaw.value = response.data
  }
}

const handleRemoveByIdentifier = async (identifier: string) => {
  const response = await FieldOfLawService.removeFieldOfLaw(
    props.documentUnitUuid,
    identifier
  )

  if (response.data) {
    selectedFieldsOfLaw.value = response.data
  }
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
      <div class="bg-white flex flex-1 flex-col p-20">
        <div class="p-20">
          <FieldOfLawSelectionList
            :selected-fields-of-law="selectedFieldsOfLaw"
            @linked-field:clicked="handleLinkedFieldClicked"
            @node-clicked="handleNodeClicked"
            @remove-from-list="handleRemoveByIdentifier"
          ></FieldOfLawSelectionList>
        </div>
        <div class="p-20">
          <FieldOfLawSearch />
        </div>
      </div>
      <div class="bg-white flex-1 p-20">
        <FieldOfLawTree
          :clicked-identifier="clickedIdentifier"
          :selected-nodes="selectedFieldsOfLaw"
          @add-to-list="handleAdd"
          @linked-field:clicked="handleLinkedFieldClicked"
          @remove-from-list="handleRemoveByIdentifier"
          @reset-clicked-node="handleResetClickedNode"
        ></FieldOfLawTree>
      </div>
    </div>
  </div>
</template>
