<script lang="ts" setup>
import { computed, nextTick, ref } from "vue"
import FieldOfLawSummary from "@/components/FieldOfLawSummary.vue"
import TextButton from "@/components/input/TextButton.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  title: string
  dataSet: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
}>()

const titleRef = ref<HTMLElement | null>(null)

const isExpanded = ref(false)

const expandButtonLabel = computed(() => {
  return props.dataSet.length > 0 ? "Weitere Angabe" : "Sachgebiete"
})

function removeNode(node: FieldOfLaw) {
  emit("node:remove", node)
}

async function toggleContentVisibilityOnButton(event: Event) {
  isExpanded.value = !isExpanded.value
  if (!isExpanded.value) {
    // Scroll back up to title - this is needed because field of law edit can be very long
    await nextTick()
    titleRef.value?.scrollIntoView({ block: "nearest" })
  }
  event.stopPropagation()
}
</script>

<template>
  <div class="flex w-full items-start justify-between bg-white">
    <div class="flex w-full flex-col">
      <h2 ref="titleRef" class="ds-label-01-bold">
        {{ title }}
      </h2>
      <FieldOfLawSummary
        v-if="!isExpanded"
        :data="dataSet"
        @node:remove="removeNode"
      />
    </div>
  </div>

  <div v-if="isExpanded" class="mt-16 flex flex-col items-start gap-24">
    <slot />
  </div>

  <TextButton
    v-if="isExpanded"
    aria-label="Fertig"
    button-type="tertiary"
    class="my-16"
    label="Fertig"
    size="small"
    @click="toggleContentVisibilityOnButton"
  />

  <TextButton
    v-else
    aria-label="Weitere Angabe"
    button-type="tertiary"
    :icon="IconAdd"
    :label="expandButtonLabel"
    size="small"
    @click="toggleContentVisibilityOnButton"
  />
</template>
