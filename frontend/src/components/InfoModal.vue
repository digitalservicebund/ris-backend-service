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
    :class="[
      staticContainerClass,
      modalAttribute.borderClass,
      modalAttribute.backgroundColorClass,
    ]"
  >
    <ModalIcon :aria-label="ariaLabelIcon" />

    <div class="flex flex-col">
      <span class="ds-label-02-bold">{{ title }}</span>
      <!-- eslint-disable vue/no-v-html -->
      <div v-if="isArray" class="ds-body-01-reg">
        <ul class="list-disc">
          <li v-for="(desc, index) in description" :key="index">{{ desc }}</li>
        </ul>
      </div>
      <!--      <span v-else class="ds-body-01-reg" v-html="description"></span>-->
    </div>
  </div>
</template>
