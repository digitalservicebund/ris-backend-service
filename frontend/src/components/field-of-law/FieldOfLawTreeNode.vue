<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { NodeHelperInterface } from "@/components/field-of-law/fieldOfLawNode"
import FlexContainer from "@/components/FlexContainer.vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import Tooltip from "@/components/Tooltip.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconArrowUp from "~icons/ic/baseline-keyboard-arrow-up"

interface Props {
  node: FieldOfLaw
  modelValue: FieldOfLaw[]
  showNorms: boolean
  nodeHelper: NodeHelperInterface
  searchResults?: FieldOfLaw[]
  expandValues: FieldOfLaw[]
  isRoot?: boolean
  rootChild?: boolean
  selectedNode?: FieldOfLaw
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "node:select": [node: FieldOfLaw]
  "node:unselect": [node: FieldOfLaw]
  "node:expand": [node: FieldOfLaw]
  "node:collapse": [node: FieldOfLaw]
  "linked-field:select": [node: FieldOfLaw]
  "selected-node:reset": []
}>()

const isExpanded = ref(false)
const children = ref<FieldOfLaw[]>([])
const isSearchCandidate = ref<boolean>(false)
const isSelected = computed({
  get: () =>
    props.modelValue.some(
      ({ identifier }) => identifier === props.node.identifier,
    ),
  set: (value) => {
    if (value) {
      emit("node:select", props.node)
    } else {
      emit("node:unselect", props.node)
    }
  },
})

function toggleExpanded() {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    emit("node:expand", props.node)
  } else {
    emit("node:collapse", props.node)
    if (props.selectedNode && props.rootChild) {
      emit("selected-node:reset")
    }
  }
}

watch(
  props,
  () => {
    if (props.expandValues.length > 0) {
      isExpanded.value = props.expandValues.some(
        (expandedNode) => expandedNode.identifier == props.node.identifier,
      )
    }
  },
  { immediate: true },
)

watch(
  props,
  async () => {
    if (props.searchResults) {
      props.searchResults.forEach((result) => {
        if (result.identifier == props.node.identifier) {
          isSearchCandidate.value = true
        }
      })
    }

    if (props.selectedNode && props.isRoot) {
      children.value = await props.nodeHelper.getFilteredChildren(
        props.node,
        props.expandValues,
      )
    } else if (props.isRoot) {
      children.value = await props.nodeHelper.getChildren(props.node)
    }
  },
  { immediate: true },
)
watch(
  isExpanded,
  async () => {
    if (isExpanded.value) {
      children.value = await props.nodeHelper.getChildren(props.node)
    }
  },
  { immediate: true },
)
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <FlexContainer class="min-h-36" flex-direction="flex-row">
      <div v-if="node.hasChildren">
        <div v-if="isExpanded">
          <Tooltip text="Zuklappen">
            <button
              id="collapse-button"
              :aria-label="node.text + ' einklappen'"
              class="w-icon rounded-full bg-blue-200 text-blue-800"
              @click="toggleExpanded"
            >
              <IconArrowUp />
            </button>
          </Tooltip>
        </div>
        <div v-else>
          <Tooltip text="Aufklappen">
            <button
              id="expand-button"
              :aria-label="node.text + ' aufklappen'"
              class="w-icon rounded-full bg-blue-200 text-blue-800"
              @click="toggleExpanded"
            >
              <IconArrowDown />
            </button>
          </Tooltip>
        </div>
      </div>
      <span v-else class="pl-[1.3333em]" />
      <div v-if="!props.isRoot" data-testid>
        <Checkbox
          :id="`field-of-law-node-${node.identifier}`"
          v-model="isSelected"
          :aria-label="
            node.identifier +
            ' ' +
            node.text +
            (isSelected ? ' entfernen' : ' hinzufügen')
          "
          class="ds-checkbox-mini ml-12 bg-white"
          :data-testid="`field-of-law-node-${node.identifier}`"
        />
      </div>

      <div>
        <div class="flex flex-col">
          <div class="ds-label-02-reg flex flex-row">
            <div v-if="!props.isRoot" class="pl-6">
              <span
                class="whitespace-nowrap p-2"
                :class="isSearchCandidate ? 'bg-yellow-300' : ''"
              >
                {{ node.identifier }} |
              </span>
            </div>
            {{ node.text }}
          </div>
        </div>
        <FlexContainer
          v-if="showNorms"
          class="pb-6 pl-8"
          flex-direction="flex-col"
        >
          <FlexContainer
            class="text-[14px] text-[#66522e]"
            flex-wrap="flex-wrap"
          >
            <span v-for="(norm, idx) in node.norms" :key="idx">
              <strong>{{ norm.singleNormDescription }}</strong>
              {{ norm.abbreviation
              }}{{ idx < node.norms.length - 1 ? ",&nbsp;" : "" }}
            </span>
          </FlexContainer>
        </FlexContainer>
      </div>
    </FlexContainer>
    <div v-if="isExpanded">
      <FieldOfLawTreeNode
        v-for="child in children"
        :key="child.identifier"
        class="pl-36"
        expand-if-selected
        :expand-values="expandValues"
        :model-value="modelValue"
        :node="child"
        :node-helper="nodeHelper"
        :root-child="props.isRoot"
        :search-results="searchResults"
        :selected-node="selectedNode"
        :show-norms="showNorms"
        @linked-field:select="emit('linked-field:select', $event)"
        @node:collapse="emit('node:collapse', $event)"
        @node:expand="emit('node:expand', $event)"
        @node:select="emit('node:select', $event)"
        @node:unselect="emit('node:unselect', $event)"
        @selected-node:reset="emit('selected-node:reset')"
      />
    </div>
  </FlexContainer>
</template>
