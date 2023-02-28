<script lang="ts" setup>
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{ node: FieldOfLawNode }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
  (event: "node-clicked"): void
}>()
</script>

<template>
  <div class="flex">
    <div class="flex flex-col grow">
      <div class="flex">
        <div class="label-02-reg pt-8 text-blue-800">
          <span
            class="link"
            @click="emit('node-clicked')"
            @keyup.enter="emit('node-clicked')"
          >
            {{ props.node.identifier }}
          </span>
        </div>
      </div>
      <div class="grow label-03-reg pb-16 pt-4 text-blue-800">
        {{ props.node.text }}
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

<style lang="scss" scoped>
.link {
  cursor: pointer;
  text-decoration: underline;

  &:active {
    text-decoration-thickness: 4px;
  }

  &:focus {
    border: 4px solid #004b76;
  }
}
</style>
