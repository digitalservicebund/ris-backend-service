<script lang="ts" setup>
import { computed } from "vue"
import { InfoStatus } from "@/shared/components/enumInfoStatus"

const props = withDefaults(
  defineProps<{
    ariaLabel?: string
    title: string
    description?: string
    status?: InfoStatus
  }>(),
  {
    ariaLabel: "Infomodal",
    description: "",
    status: InfoStatus.ERROR,
  }
)
type ModalAttribute = {
  borderClass: string
  backgroundColorClass: string
  textColorClass: string
  icon: string
}
const staticContainerClass =
  "border-l-[0.125rem] flex gap-[0.625rem] px-[1.25rem] py-[1.125rem] w-full"
const staticIconClass = "material-icons pt-1"
const modalAttribute = computed((): ModalAttribute => {
  switch (props.status) {
    case InfoStatus.SUCCEED:
      return {
        borderClass: "border-l-green-700",
        backgroundColorClass: "bg-white",
        textColorClass: "text-green-700",
        icon: "done",
      }
    default:
      return {
        borderClass: "border-l-red-800",
        backgroundColorClass: "bg-red-200",
        textColorClass: "text-red-800",
        icon: "error",
      }
  }
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
    <span
      :aria-label="ariaLabelIcon"
      :class="[staticIconClass, modalAttribute.textColorClass]"
      >{{ modalAttribute.icon }}</span
    >

    <div class="flex flex-col">
      <span class="label-02-bold">{{ title }}</span>
      <span class="body-01-reg">{{ description }}</span>
    </div>
  </div>
</template>
