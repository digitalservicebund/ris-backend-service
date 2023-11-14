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
  modelValue: FieldOfLawNode[]
  clickedIdentifier: string
  showNorms: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [value: FieldOfLawNode[]]
  "reset-clicked-node": []
  "toggle-show-norms": []
  "linkedField:clicked": [identifier: string]
}>()

const localModelValue = ref<FieldOfLawNode[]>(props.modelValue)
const root = ref(buildRoot())

const clicked = computed(() => props.clickedIdentifier)
watch(clicked, () => buildDirectPathTreeTo(clicked.value))
watch(
  props,
  () => {
    localModelValue.value = props.modelValue
  },
  { immediate: true },
)

watch(localModelValue, () => {
  emit("update:modelValue", localModelValue.value)
})

function handleSelect(node: FieldOfLawNode) {
  if (
    !localModelValue.value?.find(
      (entry) => entry.identifier === node.identifier,
    )
  ) {
    localModelValue.value?.push(node)
  }
}

function handleUnselect(node: FieldOfLawNode) {
  localModelValue.value = localModelValue.value?.filter(
    (entry) => entry.identifier !== node.identifier,
  )
  emit("update:modelValue", localModelValue.value)
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
      localModelValue.some(({ identifier }) => identifier === root.identifier)
    "
    :selected-nodes="localModelValue"
    :show-norms="showNorms"
    @linked-field:clicked="handleLinkedFieldClicked"
    @node:select="handleSelect"
    @node:unselect="handleUnselect"
  />
</template>
