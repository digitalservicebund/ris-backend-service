<script lang="ts" setup>
import { computed, h } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import IconDone from "~icons/ic/baseline-done"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconInfo from "~icons/ic/baseline-info"

const props = withDefaults(
  defineProps<{
    ariaLabel?: string
    title: string
    description?: string | string[]
    status?: InfoStatus
  }>(),
  {
    ariaLabel: "Infomodal",
    description: "",
    status: InfoStatus.ERROR,
  },
)
type ModalAttribute = {
  borderClass: string
  backgroundColorClass: string
}
const staticContainerClass =
  "border-l-[0.125rem] flex gap-[0.625rem] px-[1.25rem] py-[1.125rem] w-full"
const modalAttribute = computed((): ModalAttribute => {
  if (props.status === InfoStatus.SUCCEED) {
    return {
      borderClass: "border-l-green-700",
      backgroundColorClass: "bg-white",
    }
  } else if (props.status === InfoStatus.INFO) {
    return {
      borderClass: "border-l-blue-800",
      backgroundColorClass: "bg-white",
    }
  }
  return {
    borderClass: "border-l-red-800",
    backgroundColorClass: "bg-red-200",
  }
})

const ModalIcon = computed(() => {
  if (props.status === InfoStatus.SUCCEED) {
    return h(h(IconDone), {
      class: ["text-green-700"],
    })
  } else if (props.status === InfoStatus.INFO) {
    return h(h(IconInfo), {
      class: ["text-blue-800"],
    })
  }
  return h(h(IconErrorOutline), {
    class: ["text-red-800"],
  })
})

const isArray = computed(() => {
  return Array.isArray(props.description)
})

const ariaLabelIcon = props.title + " icon"
</script>

<template>
  <div
    :aria-label="ariaLabel"
    class="flex items-center"
    :class="[
      staticContainerClass,
      modalAttribute.borderClass,
      modalAttribute.backgroundColorClass,
    ]"
  >
    <ModalIcon :aria-label="ariaLabelIcon" />

    <div class="flex flex-col">
      <span class="ris-label2-bold">{{ title }}</span>
      <div
        v-if="isArray && props.description.length > 1"
        class="ris-label2-regular"
      >
        <ul class="m-0 list-disc ps-20">
          <li v-for="(desc, index) in description" :key="index">{{ desc }}</li>
        </ul>
      </div>
      <span v-else class="ris-label2-regular">{{
        isArray ? description[0] : description
      }}</span>
    </div>
  </div>
</template>
