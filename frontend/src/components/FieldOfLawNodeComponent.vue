<script lang="ts" setup>
import { computed } from "vue"
import { ROOT_ID, FieldOfLawNode } from "@/domain/fieldOfLawTree"
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
  (event: "node:unselect", subjectFieldNumber: string): void
  (event: "linkedField:clicked", subjectFieldNumber: string): void
}>()

type Token = {
  content: string
  isLink: boolean
}

const node = computed(() => props.node)

function tokenizeText(): Token[] {
  const stext = props.node.subjectFieldText
  const keywords = props.node.linkedFields
  if (!keywords) return [{ content: stext, isLink: false }]
  return stext.split(new RegExp(`(${keywords.join("|")})`)).map((part) => ({
    content: part,
    isLink: keywords.includes(part),
  }))
}

function handleTokenClick(token: Token) {
  if (!token.isLink) return
  emit("linkedField:clicked", token.content)
}

function handleToggle() {
  if (node.value.children.length > 1 || !node.value.isExpanded) {
    node.value.isExpanded = !node.value.isExpanded
  }

  if (
    node.value.isExpanded &&
    !node.value.isLeaf &&
    node.value.children.length < 2
  ) {
    FieldOfLawService.getChildrenOf(node.value.subjectFieldNumber).then(
      (response) => {
        if (!response.data) return
        node.value.children = response.data
      }
    )
  }
}
</script>

<template>
  <div
    class="flex flex-col"
    :class="node.subjectFieldNumber !== ROOT_ID ? 'pl-36' : ''"
  >
    <div class="flex flex-row">
      <div v-if="node.isLeaf" class="pl-24"></div>
      <div v-else>
        <button
          aria-label="Sachgebietsbaum aufklappen"
          class="bg-blue-200 material-icons rounded-full text-blue-800 w-icon"
          @click="handleToggle"
        >
          {{ node.isExpanded ? "remove" : "add" }}
        </button>
      </div>
      <div v-if="node.subjectFieldNumber !== ROOT_ID">
        <button
          aria-label="Sachgebiet entfernen"
          class="appearance-none border-2 focus:outline-2 h-24 hover:outline-2 ml-12 outline-0 outline-blue-800 outline-none outline-offset-[-4px] rounded-sm text-blue-800 w-24"
          @click="
            selected
              ? emit('node:unselect', node.subjectFieldNumber)
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
      <div
        v-if="node.subjectFieldNumber !== ROOT_ID"
        class="pl-8 subject-field-number"
      >
        {{ node.subjectFieldNumber }}
      </div>
      <div class="pl-6 pt-2 subject-field-text text-blue-800">
        <span
          v-for="(token, idx) in tokenizeText()"
          :key="idx"
          :class="token.isLink && 'linked-field'"
          @click="handleTokenClick(token)"
          @keyup.enter="handleTokenClick(token)"
        >
          {{ token.content }}
        </span>
      </div>
    </div>
    <div v-if="node.isExpanded && node.children.length">
      <FieldOfLawNodeComponent
        v-for="child in node.children"
        :key="child.subjectFieldNumber"
        :node="child"
        :selected="
          props.selectedSubjects.some(
            ({ subjectFieldNumber }) =>
              subjectFieldNumber === child.subjectFieldNumber
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
.subject-field-number {
  font-size: 16px;
  white-space: nowrap;
}

.subject-field-text {
  font-size: 14px;
}

.selected-icon {
  font-size: 20px;
}

.linked-field {
  cursor: pointer;
  text-decoration: underline;
}
</style>
