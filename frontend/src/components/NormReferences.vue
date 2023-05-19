<script lang="ts" setup>
import { computed } from "vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import { NormReference } from "@/domain/normReference"
import EditableList from "@/shared/components/EditableList.vue"

const props = defineProps<{
  modelValue: NormReference[] | undefined
}>()

const emit = defineEmits<{
  (event: "update:modelValue", value?: NormReference[]): void
}>()

const norms = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const defaultValue = {}
</script>

<template>
  <div class="bg-white mb-[2rem] p-16">
    <h2 class="label-02-bold mb-[1rem]">Normen</h2>
    <EditableList
      v-model="norms"
      :default-value="defaultValue"
      :edit-component="NormReferenceInput"
    />
  </div>
</template>
