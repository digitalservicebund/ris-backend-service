<script lang="ts" setup>
import { SubjectNode } from "@/domain/SubjectTree"

const props = defineProps<{ subject: SubjectNode }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
  (event: "node-clicked"): void
}>()
</script>

<template>
  <div class="flex">
    <div>
      <button
        aria-label="Auswahl im Sachgebietsbaum"
        class="deeplink p-6 text-blue-800"
        @click="emit('node-clicked')"
      ></button>
    </div>
    <div class="flex-1 flex-col pt-8">
      <div class="label-02-reg text-blue-800">
        {{ props.subject.subjectFieldNumber }}
      </div>
      <div class="label-03-reg text-blue-800">
        {{ props.subject.subjectFieldText }}
      </div>
    </div>
    <div>
      <button
        aria-label="Löschen"
        class="material-icons text-blue-800"
        @click="emit('remove-from-list')"
      >
        delete_outline
      </button>
    </div>
  </div>
</template>
