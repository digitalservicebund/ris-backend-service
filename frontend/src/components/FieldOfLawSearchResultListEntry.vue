<script lang="ts" setup>
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{ node: FieldOfLawNode }>()

const emit = defineEmits<{
  (event: "node-clicked"): void
}>()
</script>

<template>
  <div class="flex flex-row">
    <div class="label-02-reg text-blue-800">
      <span
        :aria-label="
          props.node.identifier +
          ' ' +
          props.node.text +
          ' im Sachgebietsbaum anzeigen'
        "
        class="link"
        @click="emit('node-clicked')"
        @keyup.enter="emit('node-clicked')"
      >
        {{ props.node.identifier }}
      </span>
    </div>
    <div class="font-size-14px pl-6 pt-2 text-blue-800">
      <TokenizeText
        :keywords="props.node.linkedFields ?? []"
        :text="props.node.text"
      />
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

.font-size-14px {
  font-size: 14px;
}
</style>
