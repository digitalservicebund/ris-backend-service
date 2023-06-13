<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  header?: string
  isExpanded?: boolean
  openIconName?: string
  closeIconName?: string
  headerId?: string
}

interface Emits {
  (event: "update:isExpanded", value: boolean): void
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  openIconName: "add",
  closeIconName: "horizontal_rule",
  headerId: "",
})

const emit = defineEmits<Emits>()

const localIsExpanded = ref(false)

const iconName = computed(() =>
  localIsExpanded.value ? props.closeIconName : props.openIconName
)

const ariaLabel = computed(() =>
  localIsExpanded.value ? "Zuklappen" : "Aufklappen"
)

function toggleContentVisibility(): void {
  localIsExpanded.value = !localIsExpanded.value
}

watch(
  () => props.isExpanded,
  () => (localIsExpanded.value = props.isExpanded ?? false),
  { immediate: true }
)

watch(localIsExpanded, () => emit("update:isExpanded", localIsExpanded.value))
</script>

<template>
  <div>
    <button
      :aria-labelledby="headerId"
      class="flex focus-visible:outline-blue-800 focus:outline-none justify-between w-full"
      @click="toggleContentVisibility"
    >
      <slot name="header">
        <span>{{ header }}</span>
      </slot>

      <span :aria-label="ariaLabel" class="icon material-icons">{{
        iconName
      }}</span>
    </button>

    <div v-if="localIsExpanded">
      <slot />
    </div>
  </div>
</template>
