<script lang="ts" setup>
import Button from "primevue/button"
import { computed, ref, watch } from "vue"
import IconAdd from "~icons/ic/baseline-add"
import IconHorizontalRule from "~icons/ic/baseline-horizontal-rule"

interface Props {
  header?: string
  headerClass?: string
  isExpanded?: boolean
  headerId?: string
  iconsOnLeft?: boolean
  marginLevel?: number
  preventExpandOnClick?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  headerId: "",
  headerClass: "",
  iconsOnLeft: false,
  marginLevel: 0,
})

const emit = defineEmits<{
  "update:isExpanded": [value: boolean]
}>()

const localIsExpanded = ref(false)

const ariaLabel = computed(() =>
  localIsExpanded.value ? "Zuklappen" : "Aufklappen",
)

function toggleContentVisibility(): void {
  if (props.preventExpandOnClick) return
  localIsExpanded.value = !localIsExpanded.value
}

function toggleContentVisibilityOnButton(event: Event): void {
  localIsExpanded.value = !localIsExpanded.value
  event.stopPropagation()
}

watch(
  () => props.isExpanded,
  () => (localIsExpanded.value = props.isExpanded ?? false),
  { immediate: true },
)

watch(localIsExpanded, () => emit("update:isExpanded", localIsExpanded.value))
</script>

<template>
  <!-- Ignore requirement to have a keyboard listener as it's only a convenience
  for mouse users, but keyboard users can already do the same thing by tabbing
  just fine -->
  <div>
    <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions vuejs-accessibility/click-events-have-key-events -->
    <div
      :aria-labelledby="headerId"
      class="flex w-full items-center justify-between focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      :class="{ 'cursor-pointer': !preventExpandOnClick }"
      @click.stop="toggleContentVisibility"
    >
      <Button
        v-if="props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="h-auto border-none p-0!"
        data-testid="icons-open-close"
        size="small"
        :style="{ marginLeft: `${marginLevel * 24}px` }"
        text
        @click.stop="toggleContentVisibilityOnButton"
      >
        <slot v-if="localIsExpanded" name="close-icon">
          <IconHorizontalRule />
        </slot>
        <slot v-else name="open-icon">
          <IconAdd />
        </slot>
      </Button>

      <slot name="header">
        <span :class="headerClass">{{ header }}</span>
      </slot>

      <Button
        v-if="!props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="h-auto border-none p-0!"
        data-testid="icons-open-close"
        size="small"
        text
        @click.stop="toggleContentVisibilityOnButton"
      >
        <slot v-if="localIsExpanded" name="close-icon">
          <IconHorizontalRule />
        </slot>
        <slot v-else name="open-icon">
          <IconAdd />
        </slot>
      </Button>
    </div>

    <div v-if="localIsExpanded">
      <slot />
    </div>
  </div>
</template>
