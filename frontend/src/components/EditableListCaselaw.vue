<script lang="ts" setup generic="T extends ListItem">
import type { Component, Ref } from "vue"
import { ref, watch, nextTick } from "vue"
import ListItem from "@/domain/editableListItem"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import IconArrowDown from "~icons/ic/baseline-keyboard-arrow-down"

interface Props {
  editComponent: Component
  summaryComponent?: Component
  modelValue?: T[]
  defaultValue: T
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  modelValue: () => [],
})

const emit = defineEmits<{
  "update:modelValue": [value: T[]]
}>()

const modelValueList = ref<T[]>([...props.modelValue]) as Ref<T[]>
const elementList = ref<HTMLElement[]>([])

const editIndex = ref<number | undefined>()

const focusFirstFocusableElementOfCurrentEditElement = async () => {
  await nextTick()

  if (!editIndex.value) {
    return
  }

  const editElement = elementList.value[editIndex.value]
  editElement
    ?.querySelector<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    )
    ?.focus()
}
watch(editIndex, focusFirstFocusableElementOfCurrentEditElement)

function setEditIndex(index?: number) {
  editIndex.value = index
}

function cancelEdit() {
  setEditIndex()
}

function addNewListEntry() {
  const { defaultValue } = props
  modelValueList.value.push(
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue,
  )

  editIndex.value = modelValueList.value.length - 1
}

function removeListEntry(index: number) {
  modelValueList.value.splice(index, 1)

  if (
    editIndex.value !== undefined &&
    modelValueList.value.length !== 0 &&
    index !== 0
  ) {
    editIndex.value -= 1
  }

  emit(
    "update:modelValue",
    [...props.modelValue].filter((_, i) => i !== index),
  )
}

function updateModel() {
  setEditIndex()
  emit("update:modelValue", modelValueList.value)
}

watch(
  () => props.modelValue,
  () => {
    modelValueList.value = modelValueList.value.map((value, index) =>
      index == editIndex.value ? value : props.modelValue[index],
    )
  },
  { immediate: true, deep: true },
)

watch(
  () => modelValueList,
  () => {
    if (modelValueList.value.length == 0) addNewListEntry()
  },
  { immediate: true, deep: true },
)
</script>

<template>
  <div class="w-full">
    <div
      v-for="(entry, index) in modelValueList"
      :key="index"
      ref="elementList"
      aria-label="Listen Eintrag"
      class="border-b-1 border-blue-500 first:border-t-1 focus:outline-none"
      :class="
        index !== editIndex
          ? 'hover:bg-gray-100 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800'
          : 'first:border-t-0 last:border-b-0'
      "
      role="presentation"
      tabindex="0"
      @click="setEditIndex(index)"
      @keypress.enter="setEditIndex(index)"
    >
      <div
        v-if="index !== editIndex"
        :key="index"
        class="group flex cursor-pointer items-center gap-8 px-2 py-16"
      >
        <component :is="summaryComponent" :data="entry" />

        <div class="flex gap-8 text-blue-800">
          <IconArrowDown />
        </div>
      </div>

      <component
        :is="editComponent"
        v-if="index === editIndex"
        v-model="modelValueList[index]"
        class="py-24"
        :model-value-list="modelValueList"
        @add-entry="updateModel"
        @cancel-edit="cancelEdit"
        @remove-list-entry="removeListEntry(index)"
      />
    </div>

    <TextButton
      v-if="editIndex === undefined"
      aria-label="Weitere Angabe"
      button-type="tertiary"
      class="mt-24 first:mt-0"
      label="Weitere Angabe"
      size="small"
      @click="addNewListEntry"
    />
  </div>
</template>
