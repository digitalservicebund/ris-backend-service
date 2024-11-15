<script lang="ts" setup>
import { ref } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import FieldOfLawSummary from "@/components/FieldOfLawSummary.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconExpandLess from "~icons/ic/baseline-expand-less"
import IconExpandMore from "~icons/ic/baseline-expand-more"

defineProps<{
  title: string
  dataSet: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
}>()

const isExpanded = ref(false)

function removeNode(node: FieldOfLaw) {
  emit("node:remove", node)
}
</script>

<template>
  <ExpandableContent
    v-model:is-expanded="isExpanded"
    class="bg-white"
    prevent-expand-on-click
  >
    <template #open-icon>
      <IconExpandMore />
    </template>

    <template #close-icon>
      <IconExpandLess />
    </template>

    <template #header>
      <div id="expandableHeader" class="flex w-full flex-col">
        <h2 class="ds-label-01-bold">
          {{ title }}
        </h2>
        <FieldOfLawSummary
          v-if="!isExpanded"
          :data="dataSet"
          @node:remove="removeNode"
        />
      </div>
    </template>

    <div class="mt-16 flex flex-col items-start gap-24">
      <slot />
    </div>
  </ExpandableContent>
</template>
