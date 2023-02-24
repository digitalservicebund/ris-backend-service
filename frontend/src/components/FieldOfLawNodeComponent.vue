<script lang="ts" setup>
import { computed } from "vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { ROOT_ID, FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

interface Props {
  selectedSubjects: FieldOfLawNode[]
  node: FieldOfLawNode
  selected: boolean
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

function handleToggle() {
  if (
    !node.value.isLeaf &&
    (node.value.children.length === 0 || node.value.inDirectPathMode)
  ) {
    FieldOfLawService.getChildrenOf(node.value.identifier).then((response) => {
      if (!response.data) return
      node.value.children = response.data
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
      <div v-if="node.isLeaf" class="pl-24"></div>
      <div v-else>
        <button
          aria-label="Sachgebietsbaum aufklappen"
          class="bg-blue-200 material-icons rounded-full text-blue-800 w-icon"
          @click="handleToggle"
        >
          {{
            props.node.isExpanded && !props.node.inDirectPathMode
              ? "remove"
              : "add"
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
      <div v-if="node.identifier !== ROOT_ID" class="identifier pl-8">
        {{ node.identifier }}
      </div>
      <div class="pl-6 pt-2 subject-field-text text-blue-800">
        <TokenizeText
          :keywords="props.node.linkedFields ?? []"
          :text="props.node.subjectFieldText"
          @link-token:clicked="handleTokenClick"
        />
      </div>
    </div>
    <div v-if="node.isExpanded && node.children.length">
      <FieldOfLawNodeComponent
        v-for="child in node.children"
        :key="child.identifier"
        :node="child"
        :selected="
          props.selectedSubjects.some(
            ({ identifier }) => identifier === child.identifier
          )
        "
        :selected-subjects="selectedSubjects"
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

.subject-field-text {
  font-size: 14px;
}

.selected-icon {
  font-size: 20px;
}
</style>
