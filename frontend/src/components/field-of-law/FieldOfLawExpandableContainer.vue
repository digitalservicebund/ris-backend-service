<script lang="ts" setup>
import { computed, nextTick, ref } from "vue"
import FieldOfLawSummary from "@/components/field-of-law/FieldOfLawSummary.vue"
import TextButton from "@/components/input/TextButton.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  fieldsOfLaw: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:select": [node: FieldOfLaw]
  editingDone: [void]
}>()

const titleRef = ref<HTMLElement | null>(null)

const isExpanded = ref(false)

const expandButtonLabel = computed(() => {
  return props.fieldsOfLaw.length > 0 ? "Weitere Angabe" : "Sachgebiete"
})

function removeNode(node: FieldOfLaw) {
  emit("node:remove", node)
}

function selectNode(node: FieldOfLaw) {
  emit("node:select", node)
  enterEditMode()
}

function enterEditMode() {
  isExpanded.value = true
}

async function exitEditMode() {
  isExpanded.value = false
  await nextTick()
  titleRef.value?.scrollIntoView({ block: "nearest" })
  emit("editingDone")
}
</script>

<template>
  <div class="flex w-full items-start justify-between bg-white">
    <div class="flex w-full flex-col">
      <div class="flex w-full flex-row items-center justify-between">
        <h2 ref="titleRef" class="ds-label-01-bold">Sachgebiete</h2>
        <TextButton
          v-if="isExpanded && fieldsOfLaw.length == 0"
          button-type="primary"
          label="Fertig"
          size="small"
          @click="exitEditMode"
        />
      </div>
      <FieldOfLawSummary
        :fields-of-law="fieldsOfLaw"
        @node:remove="removeNode"
        @node:select="selectNode"
      />
    </div>
  </div>

  <div v-if="isExpanded" class="flex flex-col items-start gap-24">
    <div
      v-if="isExpanded && fieldsOfLaw.length > 0"
      class="flex w-full flex-row justify-end"
    >
      <TextButton
        button-type="primary"
        label="Fertig"
        size="small"
        @click="exitEditMode"
      />
    </div>
    <slot />
  </div>

  <TextButton
    v-else
    button-type="tertiary"
    :icon="IconAdd"
    :label="expandButtonLabel"
    size="small"
    @click="enterEditMode"
  />
</template>
