<script lang="ts" setup generic="T extends ListItem">
import type { Component, Ref } from "vue"
import { ref, watch } from "vue"
import DataSetSummary from "@/components/DataSetSummary.vue"
import TextButton from "@/components/input/TextButton.vue"
import ListItem from "@/domain/editableListItem"
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

const editIndex = ref<number | undefined>()

/**
 * Setting the edit index, renders the edit component of the given index, the summary component is not visible
 * @param {number} index - The index of the list item to be shown in edit mode
 */
function setEditIndex(index?: number) {
  editIndex.value = index
}

/**
 * Resetting the edit index to undefined, to show all list items in summary mode
 */
function cancelEdit() {
  setEditIndex()
}

/**
 * Adds a new list item of the type given by the "defaultValue" property and sets it in edit mode
 */
function addNewListEntry() {
  const { defaultValue } = props
  modelValueList.value.push(
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue,
  )

  editIndex.value = modelValueList.value.length - 1
}

/**
 * Removes the list item, with the given index, by propagating an updated list without the list item
 * at the given index to the parent component. The edit index is resetted, to show list in summary mode.
 * @param {number} index - The index of the list item to be removed
 */
function removeEntry(index: number) {
  modelValueList.value.splice(index, 1)

  emit(
    "update:modelValue",
    [...props.modelValue].filter((_, i) => i !== index),
  )
  editIndex.value = undefined
}

/**
 * Updating the modelValue with the local modelValue list, is not propagated, until the user actively
 * decides to click the save button in edit mode. The edit index is resetted, to show list in summary mode.
 * @param {number} index - The index of the list item to be removed
 */
function updateModel() {
  setEditIndex()
  emit("update:modelValue", modelValueList.value)
}

/**
 * When the modelValue changes, it is copied to a local copy. The user can update an item in that local model value list,
 * it is not saved until the save button is clicked.
 */
watch(
  () => props.modelValue,
  () => {
    modelValueList.value = modelValueList.value.map((value, index) =>
      index == editIndex.value ? value : props.modelValue[index],
    )
  },
  {
    immediate: true,
    deep: true,
  },
)

/**
 * When the local model value list is empty, (e.g. on mount or by removing an item) a new empty list entry is added to list.
 */
watch(
  () => modelValueList,
  () => {
    if (modelValueList.value.length == 0) addNewListEntry()
  },
  {
    immediate: true,
    deep: true,
  },
)
</script>

<template>
  <div class="w-full">
    <div
      v-for="(entry, index) in modelValueList"
      :key="index"
      aria-label="Listen Eintrag"
      class="border-b-1 border-blue-300 first:border-t-1 focus:outline-none"
      :class="
        index !== editIndex
          ? 'hover:bg-blue-100 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800'
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
        class="group flex cursor-pointer gap-8 px-2 py-16"
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
        @remove-entry="removeEntry(index)"
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
