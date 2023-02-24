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
watch(clicked, () => buildDirectPathTreeTo(clicked.value))

function handleSelect(node: FieldOfLawNode) {
  emit("add-to-list", node)
}

function handleUnselect(identifier: string) {
  emit("remove-from-list", identifier)
}

function handleLinkedFieldClicked(identifier: string) {
  emit("linkedField:clicked", identifier)
}

const buildDirectPathTreeTo = async (clickedIdentifier: string) => {
  if (!clickedIdentifier) return

  const response = await FieldOfLawService.getTreeForIdentifier(
    clickedIdentifier
  )
  if (!response.data) return

  root.value.children = [response.data]
  expandAll(root.value)

  emit("reset-clicked-node")
}

function expandAll(node: FieldOfLawNode) {
  node.isExpanded = true
  node.inDirectPathMode = true
  node.children.forEach((child) => expandAll(child))
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
