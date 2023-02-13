<script lang="ts" setup>
type Subject = { id: string; text: string }

const props = defineProps<{ subject: Subject }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
}>()

function handleRemoveFromList() {
  emit("remove-from-list")
}
</script>

<template>
  <div class="flex">
    <div>
      <span class="material-icons p-6 text-blue-800">open_in_new</span>
    </div>
    <div class="flex-1 pt-8">
      <span class="leading-{22px} text-blue-800 text-{16px}">{{
        props.subject.id
      }}</span
      ><br />
      <span class="leading-{18px} text-blue-800 text-{14px}">
        {{ props.subject.text }}
      </span>
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
