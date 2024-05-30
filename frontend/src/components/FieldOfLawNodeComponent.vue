<script lang="ts" setup>
import { computed, onMounted, toRaw } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import IconAdd from "~icons/ic/baseline-add"
import IconHorizontalRule from "~icons/ic/baseline-horizontal-rule"

interface Props {
  selectedNodes: FieldOfLawNode[]
  node: FieldOfLawNode
  selected: boolean
  showNorms: boolean
  isRoot?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  "node:select": [node: FieldOfLawNode]
  "node:unselect": [node: FieldOfLawNode]
  "linkedField:clicked": [identifier: string]
}>()

const node = computed(() => props.node)

const fieldOfLawSelected = computed({
  get: () => props.selected,
  set: (value) => {
    value ? emit("node:select", node.value) : emit("node:unselect", node.value)
  },
})

function handleTokenClick(tokenContent: string) {
  emit("linkedField:clicked", tokenContent)
}

async function handleToggle() {
  await getChildren()
  if (node.value.inDirectPathMode) {
    node.value.inDirectPathMode = false
  } else {
    node.value.isExpanded = !node.value.isExpanded
  }
}

async function getChildren() {
  if (props.node.hasChildren) {
    let childToReattach: FieldOfLawNode
    if (node.value.children.length > 0) {
      // can only happen if inDirectPathMode
      childToReattach = toRaw(node.value.children[0])
    }
    await FieldOfLawService.getChildrenOf(node.value.identifier).then(
      (response) => {
        if (!response.data) return
        node.value.children = response.data

        if (!childToReattach) return
        const parentToReattachTo = node.value.children.find(
          (node) => node.identifier === childToReattach.identifier,
        )
        if (!parentToReattachTo) return
        parentToReattachTo.children = childToReattach.children
        parentToReattachTo.isExpanded = false
        parentToReattachTo.inDirectPathMode = true
      },
    )
  }
}

onMounted(async () => {
  await getChildren()
})
</script>

<template>
  <FlexContainer
    :class="!props.isRoot ? 'pl-36' : ''"
    flex-direction="flex-col"
  >
    <FlexContainer flex-direction="flex-row">
      <div v-if="!node.hasChildren" class="pl-24"></div>
      <div v-else>
        <button
          :aria-label="
            node.text +
            `${props.node.isExpanded ? ' einklappen' : ' aufklappen'}`
          "
          class="w-icon rounded-full bg-blue-200 text-blue-800"
          @click="handleToggle"
        >
          <slot v-if="props.node.isExpanded" name="close-icon">
            <IconHorizontalRule />
          </slot>
          <slot v-else name="open-icon">
            <IconAdd />
          </slot>
        </button>
      </div>
      <div v-if="!props.isRoot">
        <Checkbox
          id="fieldOfLawSelected"
          v-model="fieldOfLawSelected"
          :aria-label="
            node.identifier +
            ' ' +
            node.text +
            (selected ? ' entfernen' : ' hinzufügen')
          "
          class="ds-checkbox-mini ml-8 bg-white"
        />
      </div>
      <div>
        <div class="flex flex-col">
          <div class="flex flex-row">
            <div
              v-if="!props.isRoot"
              class="whitespace-nowrap pl-8 text-[16px]"
            >
              {{ node.identifier }}
            </div>
            <div class="pl-6 pt-2 text-[14px] text-blue-800">
              <TokenizeText
                :keywords="props.node.linkedFields ?? []"
                :text="props.node.text"
                @link-token:clicked="handleTokenClick"
              />
            </div>
          </div>
        </div>
        <FlexContainer
          v-if="showNorms"
          class="pb-6 pl-8"
          flex-direction="flex-col"
        >
          <FlexContainer class="flex-wrap text-[14px] text-[#66522e]">
            <span v-for="(norm, idx) in node.norms" :key="idx">
              <strong>{{ norm.singleNormDescription }}</strong>
              {{ norm.abbreviation
              }}{{ idx < node.norms.length - 1 ? ",&nbsp;" : "" }}
            </span>
          </FlexContainer>
        </FlexContainer>
      </div>
    </FlexContainer>
    <div v-if="node.isExpanded && node.children?.length">
      <FieldOfLawNodeComponent
        v-for="child in node.children"
        :key="child.identifier"
        :node="child"
        :selected="
          props.selectedNodes.some(
            ({ identifier }) => identifier === child.identifier,
          )
        "
        :selected-nodes="selectedNodes"
        :show-norms="showNorms"
        @linked-field:clicked="emit('linkedField:clicked', $event)"
        @node:select="emit('node:select', $event)"
        @node:unselect="emit('node:unselect', $event)"
      />
    </div>
  </FlexContainer>
</template>
