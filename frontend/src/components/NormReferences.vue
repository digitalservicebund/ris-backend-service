<script lang="ts" setup>
import { computed } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import { NormReference } from "@/domain/normReference"
import EditableList from "@/shared/components/EditableList.vue"

const props = defineProps<{
  norms?: NormReference[]
}>()

const emit = defineEmits<{
  (e: "updateValue", updatedValue: NormReference[]): Promise<void>
}>()

const norms = computed({
  get: () => {
    return props.norms ? props.norms : []
  },
  set: (value) => {
    emit("updateValue", value)
  },
})

const defaultValue = {}
</script>

<template>
  <ExpandableDataSet as-column :data-set="norms" title="Normen">
    <EditableList
      v-model="norms"
      :default-value="defaultValue"
      :edit-component="NormReferenceInput"
    />
  </ExpandableDataSet>
</template>
