<script lang="ts" setup>
import { computed, toRaw } from "vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { ROOT_ID, FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

interface Props {
  selectedNodes: FieldOfLawNode[]
  node: FieldOfLawNode
  selected: boolean
  showNorms: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (event: "node:toggle", node: FieldOfLawNode): void
  (event: "node:select", node: FieldOfLawNode): void
  (event: "node:unselect", identifier: string): void
  (event: "linkedField:clicked", identifier: string): void
}>()

const node = computed(() => props.node)

function handleTokenClick(tokenContent: string) {
  emit("linkedField:clicked", tokenContent)
}

function canLoadMoreChildren() {
  return node.value.childrenCount > node.value.children.length
}

function handleToggle() {
  if (canLoadMoreChildren()) {
    let childToReattach: FieldOfLawNode
    if (node.value.children.length > 0) {
      // can only happen if inDirectPathMode
      childToReattach = toRaw(node.value.children[0])
    }
    FieldOfLawService.getChildrenOf(node.value.identifier).then((response) => {
      if (!response.data) return
      node.value.children = response.data
      if (!childToReattach) return
      const parentToReattachTo = node.value.children.find(
        (node) => node.identifier === childToReattach.identifier
      )
      if (!parentToReattachTo) return
      parentToReattachTo.children = childToReattach.children
      parentToReattachTo.isExpanded = true
      parentToReattachTo.inDirectPathMode = true
    })
  }
  if (node.value.inDirectPathMode) {
    node.value.inDirectPathMode = false
  } else {
    node.value.isExpanded = !node.value.isExpanded
  }
}
</script>

<template>
  <div
    class="flex flex-col"
    :class="node.identifier !== ROOT_ID ? 'pl-36' : ''"
  >
    <div class="flex flex-row">
      <div v-if="node.childrenCount === 0" class="pl-24"></div>
      <div v-else>
        <button
          aria-label="Sachgebietsbaum aufklappen"
          class="bg-blue-200 material-icons rounded-full text-blue-800 w-icon"
          @click="handleToggle"
        >
          {{
            canLoadMoreChildren() || !props.node.isExpanded ? "add" : "remove"
          }}
        </button>
      </div>
      <div v-if="node.identifier !== ROOT_ID">
        <button
          aria-label="Sachgebiet entfernen"
          class="align-top appearance-none border-2 focus:outline-2 h-24 hover:outline-2 ml-12 outline-0 outline-blue-800 outline-none outline-offset-[-4px] rounded-sm text-blue-800 w-24"
          @click="
            selected
              ? emit('node:unselect', node.identifier)
              : emit('node:select', node)
          "
        >
          <span
            v-if="selected"
            aria-label="Sachgebiet entfernen"
            class="material-icons selected-icon"
          >
            done
          </span>
        </button>
      </div>
      <div>
        <div class="flex flex-col">
          <div class="flex flex-row">
            <div v-if="node.identifier !== ROOT_ID" class="identifier pl-8">
              {{ node.identifier }}
            </div>
            <div class="font-size-14px pl-6 pt-2 text-blue-800">
              <TokenizeText
                :keywords="props.node.linkedFields ?? []"
                :text="props.node.text"
                @link-token:clicked="handleTokenClick"
              />
            </div>
          </div>
        </div>
        <div v-if="showNorms" class="flex flex-col pb-6 pl-8">
          <div class="flex flex-row flex-wrap font-size-14px">
            <span v-for="(norm, idx) in node.norms" :key="idx">
              <strong>{{ norm.abbreviation }}</strong>
              {{ norm.singleNormDescription
              }}{{ idx < node.norms.length - 1 ? ",&nbsp;" : "" }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="node.isExpanded && node.children.length">
      <FieldOfLawNodeComponent
        v-for="child in node.children"
        :key="child.identifier"
        :node="child"
        :selected="
          props.selectedNodes.some(
            ({ identifier }) => identifier === child.identifier
          )
        "
        :selected-nodes="selectedNodes"
        :show-norms="showNorms"
        @linked-field:clicked="emit('linkedField:clicked', $event)"
        @node:select="emit('node:select', $event)"
        @node:unselect="emit('node:unselect', $event)"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.identifier {
  font-size: 16px;
  white-space: nowrap;
}

// TODO use tailwind instead
.font-size-14px {
  font-size: 14px;
}

.selected-icon {
  font-size: 20px;
}
</style>
