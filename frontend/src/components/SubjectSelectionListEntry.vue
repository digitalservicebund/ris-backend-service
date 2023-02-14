<script lang="ts" setup>
import { SubjectNode } from "@/domain/SubjectTree"

const props = defineProps<{ subject: SubjectNode }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
  (event: "select-node"): void
}>()

function handleRemoveFromList() {
  emit("remove-from-list")
}

function handleSelectNode() {
  // console.log("list entry: select node")
  emit("select-node")
}
</script>

<template>
  <div class="flex">
    <div>
      <button
        class="material-icons p-6 text-blue-800"
        @click="handleSelectNode"
      >
        open_in_new
      </button>
    </div>
    <div class="flex-1 flex-col pt-8">
      <div class="label-02-reg text-blue-800">{{ props.subject.id }}</div>
      <div class="label-03-reg text-blue-800">
        {{ props.subject.stext }}
      </div>
    </div>
    <div>
      <button
        class="material-icons text-blue-800"
        @click="handleRemoveFromList"
      >
        delete_outline
      </button>
    </div>
  </div>
</template>
