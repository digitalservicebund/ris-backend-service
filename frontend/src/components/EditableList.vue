<script lang="ts" setup generic="T extends ListItem">
import Button from "primevue/button"
import type { Component, Ref } from "vue"
import { ref, watch, computed, nextTick, onBeforeUpdate } from "vue"
import Tooltip from "./Tooltip.vue"
import DefaultSummary from "@/components/DefaultSummary.vue"
import { useScroll } from "@/composables/useScroll"
import ListItem from "@/domain/editableListItem" // NOSONAR: import is needed for extension
import IconEdit from "~icons/ic/outline-edit"
import IconAdd from "~icons/material-symbols/add"

interface Props {
  editComponent?: Component
  summaryComponent?: Component
  modelValue?: T[]
  createEntry: () => T
}

const props = withDefaults(defineProps<Props>(), {
  editComponent: undefined,
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
const focusAnchors = ref<Map<string, HTMLElement>>(new Map())
const { scrollNearestRefIntoViewport } = useScroll()

/**
 * Computed mergedValues is a computed helper list that ensures
 * the update of the modelValue does not affect a local new value.
 */
const mergedValues = computed(() => {
  return localNewEntry.value
    ? [...modelValueList.value, localNewEntry.value]
    : [...modelValueList.value]
})

function setEditEntry(entry?: T) {
  editEntry.value = entry
}

function isEditEntry(entry: T) {
  return editEntry.value?.localId === entry.localId
}

async function cancelEdit() {
  await toggleNewEntry(false)
  await scrollNearestRefIntoViewport(editableListContainer)
}

async function removeEntry(entry: T) {
  const updatedEntries =
    props.modelValue?.filter((item) => item.localId !== entry.localId) ?? []
  emit("update:modelValue", updatedEntries)
  setEditEntry()
  await scrollNearestRefIntoViewport(editableListContainer)
}

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
  if (!localNewEntry.value) return

  const anchor = focusAnchors.value.get(localNewEntry.value.localId)
  anchor?.focus()
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
 * Watch modelValue and update local copy
 */
watch(
  () => props.modelValue,
  (newValue) => {
    modelValueList.value = [...newValue].map((item) =>
      editEntry.value?.localId === item.localId ? editEntry.value : item,
    )
  },
  { immediate: true, deep: true },
)

/**
 * Watch for empty modelValueList to automatically show a new entry
 */
watch(
  () => modelValueList.value,
  async () => {
    if (modelValueList.value.length === 0 && !localNewEntry.value) {
      await toggleNewEntry(true)
    }
  },
  { immediate: true, deep: true },
)

onBeforeUpdate(() => {
  focusAnchors.value.clear()
})

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
      v-for="entry in mergedValues"
      :key="entry.localId"
      aria-label="Listen Eintrag"
    >
      <div
        v-if="!isEditEntry(entry)"
        class="group flex gap-8 border-b-1 border-blue-300 py-16"
        :class="{ 'border-t-1': mergedValues.indexOf(entry) === 0 }"
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
            aria-label="Eintrag bearbeiten"
            :data-testid="`list-entry-${entry.localId}`"
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
          >
            <template #icon> <IconEdit /> </template>
          </Button>
        </Tooltip>
      </div>

      <!-- hidden focus anchor -->
      <div
        :ref="
          (el) => {
            if (el && entry.localId)
              focusAnchors.set(entry.localId, el as HTMLElement)
          }
        "
        class="sr-only absolute h-0 w-0 overflow-hidden"
        tabindex="-1"
      />

      <slot
        v-if="isEditEntry(entry)"
        :model-value-list="modelValueList"
        name="edit"
        :value="entry"
        @add-entry="updateModel"
        @cancel-edit="cancelEdit"
        @remove-entry="removeEntry(entry as T)"
        @update:value="
          (a: T) => {
            const idx = mergedValues.findIndex((e) => e.localId === a.localId)
            if (idx !== -1) mergedValues[idx] = a
          }
        "
      >
        <component
          :is="editComponent"
          v-if="editComponent"
          v-model="
            mergedValues[
              mergedValues.findIndex((e) => e.localId === entry.localId)
            ]
          "
          class="py-24"
          :class="{
            'pt-0':
              mergedValues.findIndex((e) => e.localId === entry.localId) === 0,
          }"
          :model-value-list="modelValueList"
          @add-entry="updateModel"
          @cancel-edit="cancelEdit"
          @remove-entry="removeEntry(entry as T)"
        />
      </slot>
    </div>

    <Button
      v-if="!editEntry"
      aria-label="Weitere Angabe"
      class="my-24 first:mt-0"
      label="Weitere Angabe"
      severity="secondary"
      size="small"
      @click="toggleNewEntry(true)"
    >
      <template #icon> <IconAdd /> </template>
    </Button>
  </div>
</template>
