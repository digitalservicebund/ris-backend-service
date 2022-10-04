<script lang="ts" setup>
import { computed } from "vue"
import { InfoStatus } from "@/enum/enumInfoStatus"

const props = withDefaults(
  defineProps<{
    title: string
    description?: string
    status?: InfoStatus
  }>(),
  {
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
const staticContainerClass = "border-l-8 flex gap-8 px-4 py-4 w-full"
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
</script>

<template>
  <div
    :class="[
      staticContainerClass,
      modalAttribute.borderClass,
      modalAttribute.backgroundColorClass,
    ]"
  >
    <span :class="[staticIconClass, modalAttribute.textColorClass]">{{
      modalAttribute.icon
    }}</span>

    <div class="flex flex-col">
      <span class="font-bold">{{ title }}</span>
      {{ description }}
    </div>
  </div>
</template>
