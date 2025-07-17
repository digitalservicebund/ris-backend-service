<script lang="ts" setup>
import { computed } from "vue"
import EditableListItem from "@/domain/editableListItem"
import IcBaselineTranslate from "~icons/ic/baseline-translate"

defineProps<{
  data: EditableListItem
}>()

const iconComponent = computed(() => {
  return IcBaselineTranslate
})
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <component :is="iconComponent" class="mr-8" />
      <div class="ris-label1-regular mr-8 max-w-[calc(30vw)] truncate">
        {{ data?.renderSummary }}
      </div>
    </div>
  </div>
</template>
