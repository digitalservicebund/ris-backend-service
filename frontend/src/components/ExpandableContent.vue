<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  header?: string
  headerClass?: string
  isExpanded?: boolean
  openIconName?: string
  closeIconName?: string
  headerId?: string
  iconsOnLeft?: boolean
  marginLevel?: number
  preventExpandOnClick?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  openIconName: "add",
  closeIconName: "horizontal_rule",
  headerId: "",
  headerClass: "",
  iconsOnLeft: false,
  marginLevel: 0,
})

const emit = defineEmits<{
  "update:isExpanded": [value: boolean]
}>()

const localIsExpanded = ref(false)

const iconName = computed(() =>
  localIsExpanded.value ? props.closeIconName : props.openIconName,
)

const ariaLabel = computed(() =>
  localIsExpanded.value ? "Zuklappen" : "Aufklappen",
)

function toggleContentVisibility(event: Event): void {
  if (
    !props.preventExpandOnClick ||
    (event.target &&
      (event.target as HTMLElement).classList.contains("material-icons"))
  ) {
    localIsExpanded.value = !localIsExpanded.value
  }
  event.stopPropagation()
}

watch(
  () => props.isExpanded,
  () => (localIsExpanded.value = props.isExpanded ?? false),
  { immediate: true },
)

watch(localIsExpanded, () => emit("update:isExpanded", localIsExpanded.value))

const sectionMargins: { [key: number]: string } = {
  0: "ml-[0px]",
  1: "ml-[22px]",
  2: "ml-[44px]",
  3: "ml-[66px]",
  4: "ml-[88px]",
  5: "ml-[108px]",
  6: "ml-[128px]",
  7: "ml-[148px]",
  8: "ml-[168px]",
}
</script>

<template>
  <!-- Ignore requirement to have a keyboard listener as it's only a convenience
  for mouse users, but keyboard users can already do the same thing by tabbing
  just fine -->
  <!-- eslint-disable vuejs-accessibility/click-events-have-key-events -->
  <div>
    <div
      :aria-labelledby="headerId"
      class="flex w-full justify-between focus:outline-none focus-visible:outline-blue-800"
      @click="toggleContentVisibility"
    >
      <button
        v-if="props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="icon material-icons"
        :class="`${sectionMargins[marginLevel]}`"
        data-testid="icons-open-close"
        @click="toggleContentVisibility"
      >
        {{ iconName }}
      </button>

      <slot name="header">
        <span :class="headerClass">{{ header }}</span>
      </slot>

      <button
        v-if="!props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="icon material-icons"
        data-testid="icons-open-close"
        @click="toggleContentVisibility"
      >
        {{ iconName }}
      </button>
    </div>

    <div v-if="localIsExpanded">
      <slot />
    </div>
  </div>
</template>
