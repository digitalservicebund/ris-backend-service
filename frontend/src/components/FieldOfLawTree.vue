<script lang="ts" setup>
import { computed, watch, ref } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { buildRoot, FieldOfLawNode } from "@/domain/fieldOfLawTree"
import FieldOfLawService from "@/services/fieldOfLawService"

const props = defineProps<{
  selectedSubjects: FieldOfLawNode[]
  clickedSubjectFieldNumber: string
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: FieldOfLawNode): void
  (event: "remove-from-list", subjectFieldNumber: string): void
  (event: "reset-clicked-node"): void
  (event: "linkedField:clicked", subjectFieldNumber: string): void
}>()

const root = ref(buildRoot())

const clicked = computed(() => props.clickedSubjectFieldNumber)
watch(clicked, () => loadedClickedFieldOfLaw(clicked.value))

function handleSelect(node: FieldOfLawNode) {
  emit("add-to-list", node)
}

function handleUnselect(subjectFieldNumber: string) {
  emit("remove-from-list", subjectFieldNumber)
}

function handleLinkedFieldClicked(subjectFieldNumber: string) {
  emit("linkedField:clicked", subjectFieldNumber)
}

const loadedClickedFieldOfLaw = async (clickedSubjectFieldNumber: string) => {
  if (!clickedSubjectFieldNumber) return

  console.log("identifier", clickedSubjectFieldNumber)

  const response = await FieldOfLawService.getTreeForNumber(
    clickedSubjectFieldNumber
  )
  if (!response.data) return

  root.value.isExpanded = true
  root.value.children = [response.data]
  expandAllChilds(root.value.children)

  emit("reset-clicked-node")
}

function expandAllChilds(children: FieldOfLawNode[]) {
  if (!children || !children.length) return

  children.forEach((child) => {
    console.log("expand", child.subjectFieldNumber)
    child.isExpanded = true
    expandAllChilds(child.children)
  })
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <FieldOfLawNodeComponent
    :key="root.subjectFieldNumber"
    :node="root"
    :selected="
      props.selectedSubjects.some(
        ({ subjectFieldNumber }) =>
          subjectFieldNumber === root.subjectFieldNumber
      )
    "
    :selected-subjects="selectedSubjects"
    @linked-field:clicked="handleLinkedFieldClicked"
    @node:select="handleSelect"
    @node:unselect="handleUnselect"
  />
</template>
