<script lang="ts" setup>
import { ref, watch, useAttrs } from "vue"
import type { Component } from "vue"

interface Props {
  component: Component
  modelValue: unknown[]
  defaultValue: unknown
}

interface Emits {
  (event: "update:modelValue", value: unknown[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const attributes = useAttrs()
const modelValueList = ref<unknown[]>([])

function addNewModelEntry() {
  const { defaultValue } = props
  const newEntry =
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue
  modelValueList.value.push(newEntry)
}

function removeModelEntry(index: number) {
  modelValueList.value.splice(index, 1)
}

watch(
  () => props.modelValue,
  () => (modelValueList.value = props.modelValue),
  { immediate: true, deep: true }
)

watch(
  modelValueList,
  () => {
    emit("update:modelValue", modelValueList.value)
  },
  { deep: true }
)
</script>

<template>
  <div v-for="(_, index) in modelValueList" :key="index">
    <component
      :is="component"
      v-model="modelValueList[index]"
      v-bind="attributes"
    />

    <slot
      v-if="index + 1 < modelValueList.length"
      name="removeButton"
      :on-click="() => removeModelEntry(index)"
    >
      <button aria-label="Eintrag Entfernen" @click="removeModelEntry(index)">
        Eintrag Entfernen
      </button>
    </slot>
  </div>

  <slot name="addButton" :on-click="addNewModelEntry">
    <button aria-label="Eintrag Hinzufügen" @click="addNewModelEntry">
      Eintrag Hinzufügen
    </button>
  </slot>
</template>
