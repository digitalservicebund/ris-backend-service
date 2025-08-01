<script lang="ts" setup>
import { computed } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  data: Reference
}>()

const primaryReference = computed(() =>
  props.data?.primaryReference ? "primär" : "sekundär",
)
const hasAmbiguousLegalPeriodicalAbbreviation = computed(
  () => !props.data?.legalPeriodical && !!props.data?.legalPeriodicalRawValue,
)
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <div class="ris-label1-regular mr-8">
        {{ data.renderSummary }}
      </div>
      <IconBadge
        background-color="bg-blue-300"
        class="mr-8"
        :label="primaryReference"
        text-color="text-blue-900"
      />
      <IconBadge
        v-if="hasAmbiguousLegalPeriodicalAbbreviation"
        background-color="bg-red-300"
        label="Mehrdeutiger Verweis"
        text-color="text-red-900"
      />
    </div>
  </div>
</template>
