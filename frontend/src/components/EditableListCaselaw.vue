<script lang="ts" setup generic="T extends ListItem">
import type { Component, Ref } from "vue"
import { ref, watch, nextTick, onMounted } from "vue"
import ListItem from "@/domain/editableListItem"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"
import IconAdd from "~icons/ic/baseline-add"
import IconEditNote from "~icons/ic/baseline-edit-note"
import IconDelete from "~icons/ic/outline-delete"

interface Props {
  editComponent: Component
  summaryComponent?: Component
  modelValue?: T[]
  defaultValue: T
  disableMultiEntry?: boolean
  addEntryLabel?: string
  noHorizontalSeparators?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  modelValue: () => [],
  disableMultiEntry: false,
  addEntryLabel: "Weitere Angabe",
  noHorizontalSeparators: false,
})

const emit = defineEmits<{
  "update:modelValue": [value: T[]]
  deleteLastEntry: [void]
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

function addNewListEntry() {
  const { defaultValue } = props
  modelValueList.value.push(
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue,
  )

  editIndex.value = modelValueList.value.length - 1
}

function removeListEntry(index: number) {
  modelValueList.value.splice(index, 1)

  if (editIndex.value !== undefined && index < editIndex.value) {
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

onMounted(() => {
  if (props.modelValue.length == 0) addNewListEntry()
})
</script>

<template>
  <div class="w-full">
    <div
      v-for="(entry, index) in modelValueList"
      :key="index"
      ref="elementList"
      aria-label="Listen Eintrag"
    >
      <div
        v-if="index !== editIndex"
        :key="index"
        class="group flex cursor-pointer items-center justify-between gap-8 border-b-1 border-gray-400 py-8 group-first:pt-0"
        :class="{ '!border-none': noHorizontalSeparators }"
      >
        <component
          :is="summaryComponent"
          class="focus:outline-none focus-visible:outline-blue-800"
          :data="entry"
          tabindex="0"
          @click="
            entry.hasForeignSource
              ? (e: Event) => e.preventDefault()
              : setEditIndex(index)
          "
          @keypress.enter="setEditIndex(index)"
        />

        <div class="flex gap-8">
          <button
            v-if="!entry.isReadOnly"
            aria-label="Eintrag bearbeiten"
            class="p-2 text-blue-800 outline-none outline-offset-2 hover:bg-blue-200 focus:outline-2 focus:outline-blue-800 active:bg-blue-500 active:outline-none"
            @click="setEditIndex(index)"
          >
            <IconEditNote />
          </button>

          <button
            aria-label="Eintrag löschen"
            class="p-2 text-blue-800 outline-none outline-offset-2 hover:bg-blue-200 focus:outline-2 focus:outline-blue-800 active:bg-blue-500 active:outline-none"
            @click="removeListEntry(index)"
          >
            <IconDelete />
          </button>
        </div>
      </div>

      <component
        :is="editComponent"
        v-else
        v-model="modelValueList[index]"
        class="py-16 group-first:pt-0"
        :model-value-list="modelValueList"
        @add-entry="updateModel"
      />
    </div>

    <button
      v-if="!disableMultiEntry && editIndex === undefined"
      aria-label="Weitere Angabe"
      class="add-button gap-0.5 mt-16 flex items-center whitespace-nowrap bg-blue-300 px-8 py-2 text-14 font-bold leading-18 text-blue-800 outline-none outline-0 outline-offset-4 outline-blue-800 hover:bg-blue-800 hover:text-white focus:outline-4"
      @click="addNewListEntry"
    >
      <IconAdd class="mr-4" />
      Weitere Angabe
    </button>
  </div>
</template>

<style lang="scss" scoped>
.add-button {
  &:focus:not(:focus-visible) {
    @apply outline-transparent;
  }
}
</style>
