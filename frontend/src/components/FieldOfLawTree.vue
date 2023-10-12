<script lang="ts" setup>
import { computed, watch, ref } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { buildRoot, getDescendants, FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"

const props = defineProps<{
  selectedNodes: FieldOfLawNode[]
  clickedIdentifier: string
  showNorms: boolean
}>()

const emit = defineEmits<{
  "add-to-list": [identifier: string]
  "remove-from-list": [identifier: string]
  "reset-clicked-node": []
  "toggle-show-norms": []
  "linkedField:clicked": [identifier: string]
}>()

const root = ref(buildRoot())

const clicked = computed(() => props.clickedIdentifier)
watch(clicked, () => buildDirectPathTreeTo(clicked.value))

function handleSelect(node: FieldOfLawNode) {
  emit("add-to-list", node.identifier)
}

function handleUnselect(identifier: string) {
  emit("remove-from-list", identifier)
}

function handleLinkedFieldClicked(identifier: string) {
  emit("linkedField:clicked", identifier)
}

const buildDirectPathTreeTo = async (clickedIdentifier: string) => {
  if (!clickedIdentifier) return

  const response =
    await FieldOfLawService.getTreeForIdentifier(clickedIdentifier)
  if (!response.data) return

  root.value.children = [response.data]
  getDescendants(root.value).forEach((node) => {
    node.isExpanded = true
    node.inDirectPathMode = true
  })

  emit("reset-clicked-node")
}

const showNormsModelValue = computed({
  get: () => props.showNorms,
  set: () => emit("toggle-show-norms"),
})
</script>

<template>
  <div class="flex flex-col justify-between">
    <h1 class="ds-heading-03-reg pb-10">Sachgebietsbaum</h1>
    <InputField
      id="showNorms"
      aria-label="Beschlussfassung mit qualifizierter Mehrheit"
      label="Normen anzeigen"
      :label-position="LabelPosition.RIGHT"
    >
      <CheckboxInput
        id="showNorms"
        v-model="showNormsModelValue"
        size="small"
      />
    </InputField>
  </div>
  <FieldOfLawNodeComponent
    :key="root.identifier"
    :node="root"
    :selected="
      props.selectedNodes.some(
        ({ identifier }) => identifier === root.identifier,
      )
    "
    :selected-nodes="selectedNodes"
    :show-norms="showNorms"
    @linked-field:clicked="handleLinkedFieldClicked"
    @node:select="handleSelect"
    @node:unselect="handleUnselect"
  />
</template>
