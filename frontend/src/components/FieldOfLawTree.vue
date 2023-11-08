<script lang="ts" setup>
import { computed, watch, ref } from "vue"
import FieldOfLawNodeComponent from "./FieldOfLawNodeComponent.vue"
import { buildRoot, getDescendants, FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

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
</script>

<template>
  <div class="flex items-center justify-between pb-10">
    <h1 class="ds-heading-03-reg">Sachgebietsbaum</h1>
    <div class="flex items-center">
      <button
        aria-label="Normen anzeigen"
        class="h-24 w-24 appearance-none rounded-sm border-2 align-top text-blue-800 outline-none outline-0 outline-offset-[-4px] outline-blue-800 hover:outline-2 focus:outline-2"
        @click="emit('toggle-show-norms')"
      >
        <span
          v-if="showNorms"
          aria-label="Sachgebiet entfernen"
          class="material-icons selected-icon"
        >
          done
        </span>
      </button>
      <span class="pl-8">Normen anzeigen</span>
    </div>
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

<style lang="scss" scoped>
.selected-icon {
  font-size: 20px;
}
</style>
