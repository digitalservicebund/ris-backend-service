<script setup lang="ts">
import { ref } from "vue"
import IconAdd from "~icons/ic/baseline-add"
import IconHorizontalRule from "~icons/ic/baseline-horizontal-rule"

interface Props {
  modelValue: boolean
  ariaLabel: string
}
const props = defineProps<Props>()
const emits = defineEmits<{
  "update:modelValue": [value?: boolean]
}>()

const localIsExpanded = ref(props.modelValue)

function toggleContentVisibility(): void {
  localIsExpanded.value = !localIsExpanded.value
  emits("update:modelValue", localIsExpanded.value)
}
</script>

<template>
  <div class="top-36 right-[-9px] z-10">
    <button
      :aria-label="`${ariaLabel} ${localIsExpanded ? 'schließen' : 'anzeigen'}`"
      class="w-icon cursor-pointer rounded-full bg-blue-800 text-white focus:outline-none focus-visible:ring-4 focus-visible:ring-white focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      @click="toggleContentVisibility"
    >
      <slot v-if="localIsExpanded" name="close-icon">
        <IconHorizontalRule class="h-20 w-20" />
      </slot>
      <slot v-else name="open-icon">
        <IconAdd class="h-20 w-20" />
      </slot>
    </button>
  </div>
</template>
