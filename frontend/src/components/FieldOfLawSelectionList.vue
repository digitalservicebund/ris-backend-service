<script lang="ts" setup>
import { ref, watch } from "vue"
import FieldOfLawListEntry from "./FieldOfLawListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{
  modelValue: FieldOfLawNode[]
}>()

const emit = defineEmits<{
  "node-clicked": [identifier: string]
  "linkedField:clicked": [identifier: string]
  "update:modelValue": [value: FieldOfLawNode[]]
}>()

const localModelValue = ref<FieldOfLawNode[]>(props.modelValue)

watch(
  props,
  () => {
    localModelValue.value = props.modelValue
  },
  { immediate: true },
)

function handleRemove(item: FieldOfLawNode) {
  localModelValue.value = localModelValue.value?.filter(
    (entry) => entry.identifier !== item.identifier,
  )
  emit("update:modelValue", localModelValue.value)
}
</script>

<template>
  <div class="pt-20">
    <div v-if="!modelValue.length">Die Liste ist aktuell leer</div>
    <div v-else>
      <FieldOfLawListEntry
        v-for="fieldOfLaw in localModelValue"
        :key="fieldOfLaw.identifier"
        :field-of-law="fieldOfLaw"
        show-bin
        @linked-field:clicked="
          (identifier) => emit('linkedField:clicked', identifier)
        "
        @node-clicked="emit('node-clicked', fieldOfLaw.identifier)"
        @remove-from-list="handleRemove(fieldOfLaw)"
      />
    </div>
  </div>
</template>
