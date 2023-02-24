<script lang="ts" setup>
import { computed, watch, ref } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { buildRoot, FieldOfLawNode } from "@/domain/fieldOfLawTree"
import FieldOfLawService from "@/services/fieldOfLawService"

const props = defineProps<{
  selectedSubjects: FieldOfLawNode[]
  clickedIdentifier: string
}>()

const emit = defineEmits<{
  (event: "add-to-list", node: FieldOfLawNode): void
  (event: "remove-from-list", identifier: string): void
  (event: "reset-clicked-node"): void
  (event: "linkedField:clicked", identifier: string): void
}>()

const root = ref(buildRoot())

const clicked = computed(() => props.clickedIdentifier)
watch(clicked, () => loadedClickedFieldOfLaw(clicked.value))

function handleSelect(node: FieldOfLawNode) {
  emit("add-to-list", node)
}

function handleUnselect(identifier: string) {
  emit("remove-from-list", identifier)
}

function handleLinkedFieldClicked(identifier: string) {
  emit("linkedField:clicked", identifier)
}

const loadedClickedFieldOfLaw = async (clickedIdentifier: string) => {
  if (!clickedIdentifier) return

  console.log("identifier", clickedIdentifier)

  const response = await FieldOfLawService.getTreeForIdentifier(
    clickedIdentifier
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
    console.log("expand", child.identifier)
    child.isExpanded = true
    expandAllChilds(child.children)
  })
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Sachgebietsbaum</h1>
  <FieldOfLawNodeComponent
    :key="root.identifier"
    :node="root"
    :selected="
      props.selectedSubjects.some(
        ({ identifier }) => identifier === root.identifier
      )
    "
    :selected-subjects="selectedSubjects"
    @linked-field:clicked="handleLinkedFieldClicked"
    @node:select="handleSelect"
    @node:unselect="handleUnselect"
  />
</template>
