<script setup lang="ts">
import { computed } from "vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import IconDone from "~icons/ic/baseline-done"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconInfo from "~icons/ic/baseline-info"

const props = defineProps<{
  status?: InfoStatus
}>()

const iconClass = computed(() => {
  if (props.status === InfoStatus.INFO) {
    return "text-blue-800"
  } else if (props.status === InfoStatus.SUCCEED) {
    return "text-green-700"
  } else {
    return "text-red-800"
  }
})

const icon = computed(() => {
  if (props.status === InfoStatus.INFO) {
    return IconInfo
  } else if (props.status === InfoStatus.SUCCEED) {
    return IconDone
  } else {
    return IconErrorOutline
  }
})
</script>

<template>
  <div :class="iconClass">
    <component :is="icon" />
  </div>
</template>
