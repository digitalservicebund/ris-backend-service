<script lang="ts" setup>
import { computed } from "vue"
import BorderNumberLinkView from "@/components/BorderNumberLinkView.vue"
import IconBadge from "@/components/IconBadge.vue"
import Definition from "@/domain/definition"
import IconError from "~icons/ic/baseline-error"
import IconBaselineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  data: Definition
}>()

const showErrorBadge = computed(() => {
  return props.data?.hasMissingRequiredFields
})
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IconBaselineDescription class="mr-8" />
      <div class="ris-label1-regular mr-8">
        <span>{{ data?.definedTerm }}</span>
        <span v-if="data?.definingBorderNumber">
          |
          <BorderNumberLinkView :border-number="data.definingBorderNumber" />
        </span>
      </div>
      <IconBadge
        v-if="showErrorBadge"
        background-color="bg-red-300"
        :icon="IconError"
        icon-color="text-red-900"
        label="Fehlende Daten"
      />
    </div>
  </div>
</template>
