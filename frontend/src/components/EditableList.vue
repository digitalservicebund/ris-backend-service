<script lang="ts" setup generic="T extends ListItem">
import Button from "primevue/button"
import type { Component, Ref } from "vue"
import { ref, watch, computed, nextTick, onBeforeUpdate } from "vue"
import Tooltip from "./Tooltip.vue"
import DefaultSummary from "@/components/DefaultSummary.vue"
import { useScroll } from "@/composables/useScroll"
import ListItem from "@/domain/editableListItem" // NOSONAR: Imported needed for generic component
import IconEdit from "~icons/ic/outline-edit"
import IconAdd from "~icons/material-symbols/add"

interface Props {
  editComponent: Component
  summaryComponent?: Component
  modelValue?: T[]
  createEntry: () => T
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DefaultSummary,
  modelValue: () => [],
})

const emit = defineEmits<{
  "update:modelValue": [value: T[]]
}>()

const editEntry = ref<T | undefined>() as Ref<T | undefined>
const modelValueList = ref<T[]>([...props.modelValue]) as Ref<T[]>
const localNewEntry = ref<T | undefined>() as Ref<T | undefined>
const editableListContainer = ref(null)
const focusAnchors = ref<HTMLElement[]>([])
const { scrollNearestRefIntoViewport } = useScroll()

/**
 * Computed mergedValues is a computed helper list that ensure the update of the modelValue does not effect a local new value
 * (Which otherwise could not be differentiated to deleted values and would be overriden). This keeps the new entry value
 * separated from the modelValue, only when saved it will move to modelValue,
 */
const mergedValues = computed(() => {
  return localNewEntry.value
    ? [...modelValueList.value, localNewEntry.value]
    : [...modelValueList.value]
})

/**
 * Setting the edit entry, renders the edit component of the given entry, the summary component is invisible
 * @param entry
 */
function setEditEntry(entry?: T) {
  editEntry.value = entry
}

/**
 * Returns if current entry is the one in edit mode
 * @param entry
 */
function isEditEntry(entry: T) {
  return editEntry.value && editEntry.value.id === entry.id
}

/**
 * Resetting the edit to undefined, to show all list items in summary mode
 */
async function cancelEdit() {
  await toggleNewEntry(false)
  await scrollNearestRefIntoViewport(editableListContainer)
}

/**
 *
 * Removes a new list item, with the given entry, by propagating an updated list without the list item
 * at the given index to the parent component. Resets edited entry reset, to show list in summary mode.
 * @param entry
 */
async function removeEntry(entry: T) {
  const updatedEntries = [...props.modelValue].filter(
    (item) => entry.id !== item.id,
  )
  emit("update:modelValue", updatedEntries)
  setEditEntry()
  await scrollNearestRefIntoViewport(editableListContainer)
}

/**
 * Updating the modelValue with the local modelValue list, is not propagated, until the user actively
 * decides to click the save button in edit mode. The edit index is reset, to show list in summary mode.
 */
async function updateModel() {
  emit("update:modelValue", mergedValues.value)
  await toggleNewEntry(true)
  await scrollNearestRefIntoViewport(editableListContainer)
}

async function handleAddFromSummary(newEntry: T) {
  localNewEntry.value = newEntry
  setEditEntry(newEntry)
}

async function resetFocus() {
  await nextTick()
  const index = mergedValues.value.findIndex(
    (item) => item.id === localNewEntry.value?.id,
  )
  if (index !== -1 && focusAnchors.value[index - 1]) {
    focusAnchors.value[index - 1].focus()
  }
}

async function toggleNewEntry(shouldDisplay: boolean) {
  if (shouldDisplay) {
    localNewEntry.value = props.createEntry()
    setEditEntry(localNewEntry.value)
    await resetFocus()
  } else {
    localNewEntry.value = undefined
    setEditEntry()
  }
}

/**
 * When the modelValue changes, it is copied to a local copy. The user can update an item in that local model value list,
 * it is not saved until the save button is clicked.
 */
watch(
  () => props.modelValue,
  (newValue) => {
    modelValueList.value = [...newValue].map((item) =>
      editEntry.value !== undefined && editEntry.value.id === item.id
        ? editEntry.value
        : item,
    )
    return modelValueList.value
  },
  {
    immediate: true,
    deep: true,
  },
)

/**
 * When the local model value list is empty, (e.g. on mount or by removing an item) a default entry is displayed
 */
watch(
  () => modelValueList,
  async () => {
    if (modelValueList.value.length == 0 && !localNewEntry.value) {
      await toggleNewEntry(true)
    }
  },
  {
    immediate: true,
    deep: true,
  },
)
// Clear the refs before each DOM update to prevent stale references
onBeforeUpdate(() => {
  focusAnchors.value = []
})
// Expose the method
defineExpose({
  toggleNewEntry,
})
</script>

<template>
  <div
    ref="editableListContainer"
    class="w-full scroll-m-64"
    data-testid="editable-list-container"
  >
    <div
      v-for="(entry, index) in mergedValues"
      :key="index"
      aria-label="Listen Eintrag"
    >
      <div
        v-if="!isEditEntry(entry)"
        :key="index"
        class="group flex gap-8 border-b-1 border-blue-300 py-16"
        :class="{ 'border-t-1': index == 0 }"
      >
        <component
          :is="summaryComponent"
          class="flex scroll-m-64"
          :data="entry"
          @add-new-entry="handleAddFromSummary"
        />

        <Tooltip text="Eintrag bearbeiten">
          <Button
            id="editable-list-select-button"
            :data-testid="`list-entry-${index}`"
            size="small"
            text
            @click="
              () => {
                toggleNewEntry(false)
                setEditEntry(entry as T)
              }
            "
            @keypress.enter="
              () => {
                toggleNewEntry(false)
                setEditEntry(entry as T)
              }
            "
            ><template #icon> <IconEdit /> </template
          ></Button>
        </Tooltip>
      </div>
      <!-- ↓↓↓ Helper: hidden focussed HTMLElement to be able to set focus into EditComponent on next tab -->
      <div
        :ref="
          (el) => {
            if (el) focusAnchors[index] = el as HTMLElement
          }
        "
        class="sr-only absolute h-0 w-0 overflow-hidden"
        tabindex="-1"
      />
      <component
        :is="editComponent"
        v-if="isEditEntry(entry)"
        v-model="mergedValues[index]"
        class="py-24"
        :class="{ 'pt-0': index == 0 }"
        :model-value-list="modelValueList"
        @add-entry="updateModel"
        @cancel-edit="cancelEdit"
        @remove-entry="removeEntry(entry as T)"
      />
    </div>

    <Button
      v-if="!editEntry"
      aria-label="Weitere Angabe"
      class="my-24 first:mt-0"
      label="Weitere Angabe"
      severity="secondary"
      size="small"
      @click="toggleNewEntry(true)"
      ><template #icon> <IconAdd /> </template
    ></Button>
  </div>
</template>
