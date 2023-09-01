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
  marginLeft?: number
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  openIconName: "add",
  closeIconName: "horizontal_rule",
  headerId: "",
  headerClass: "",
  iconsOnLeft: false,
  marginLeft: 0,
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

function toggleContentVisibility(): void {
  localIsExpanded.value = !localIsExpanded.value
}

watch(
  () => props.isExpanded,
  () => (localIsExpanded.value = props.isExpanded ?? false),
  { immediate: true },
)

watch(localIsExpanded, () => emit("update:isExpanded", localIsExpanded.value))

const sectionMargins: { [key: number]: string } = {
  0: "ml-[0px]",
  20: "ml-[20px]",
  40: "ml-[40px]",
  60: "ml-[60px]",
  80: "ml-[80px]",
  100: "ml-[100px]",
  120: "ml-[120px]",
  140: "ml-[140px]",
  160: "ml-[160px]",
}
</script>

<template>
  <div>
    <button
      :aria-labelledby="headerId"
      class="flex w-full justify-between focus:outline-none focus-visible:outline-blue-800"
      :class="`${sectionMargins[marginLeft]}`"
      @click="toggleContentVisibility"
    >
      <span
        v-if="props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="icon material-icons"
      >
        {{ iconName }}
      </span>

      <slot name="header">
        <span :class="headerClass">{{ header }}</span>
      </slot>

      <span
        v-if="!props.iconsOnLeft"
        :aria-label="ariaLabel"
        class="icon material-icons"
        >{{ iconName }}</span
      >
    </button>

    <div v-if="localIsExpanded">
      <slot />
    </div>
  </div>
</template>
