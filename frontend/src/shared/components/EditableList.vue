<script lang="ts" setup>
import type { Component } from "vue"
import { computed, nextTick, onMounted, ref, watch } from "vue"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"

interface Props {
  editComponent: Component
  summaryComponent?: Component
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  modelValue?: any[]
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  defaultValue: any
  disableMultiEntry?: boolean
  addEntryLabel?: string
  noHorizontalSeparators?: boolean
}

interface Emits {
  (event: "update:modelValue", value: undefined[]): void
  (event: "deleteLastEntry"): void
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  modelValue: () => [],
  disableMultiEntry: false,
  addEntryLabel: "Weitere Angabe",
  noHorizontalSeparators: false,
})

const emit = defineEmits<Emits>()

const modelValueList = ref<undefined[]>([])
const elementList = ref<HTMLElement[]>([])
const editIndex = ref<number | undefined>(undefined)

const currentEditElement = computed(() =>
  editIndex.value !== undefined
    ? elementList.value[editIndex.value]
    : undefined,
)

function setEditIndex(newEditIndex?: number): void {
  if (
    editIndex.value !== undefined &&
    entryIsEmpty(modelValueList.value[editIndex.value])
  ) {
    removeModelEntry(editIndex.value)

    editIndex.value =
      newEditIndex === undefined
        ? undefined
        : editIndex.value < newEditIndex
        ? newEditIndex - 1
        : newEditIndex
  } else {
    editIndex.value = newEditIndex
  }
}

function entryIsEmpty(entry: unknown): boolean {
  return typeof entry === "object"
    ? Object.values(entry ?? {}).every(entryIsEmpty)
    : !Boolean(entry)
}

function addNewModelEntry() {
  const { defaultValue } = props
  const newEntry =
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue
  modelValueList.value.push(newEntry)
  setEditIndex(modelValueList.value.length - 1)
}

function removeModelEntry(index: number) {
  modelValueList.value.splice(index, 1)

  if (editIndex.value !== undefined && index < editIndex.value) {
    editIndex.value -= 1
  }
}

async function focusFirstFocusableElementOfCurrentEditElement() {
  await nextTick()

  if (!currentEditElement.value) return

  const firstFocusableElement =
    currentEditElement.value.querySelector<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    )

  let selectedGroupElement: HTMLInputElement | null = null

  if (
    firstFocusableElement instanceof HTMLInputElement &&
    firstFocusableElement.type === "radio" &&
    firstFocusableElement.name
  ) {
    selectedGroupElement =
      currentEditElement.value.querySelector<HTMLInputElement>(
        `input[type="radio"][name="${firstFocusableElement.name}"]:checked`,
      )
  }

  if (selectedGroupElement) {
    selectedGroupElement.focus()
  } else if (firstFocusableElement) {
    firstFocusableElement.focus()
  }
}

function editFirstEntryIfOnlyOne(): void {
  if (modelValueList.value?.length == 1) setEditIndex(0)
}

onMounted(editFirstEntryIfOnlyOne)

watch(
  () => props.modelValue,
  () => (modelValueList.value = props.modelValue),
  { immediate: true, deep: true },
)

watch(
  modelValueList,
  () => {
    if (modelValueList.value.length == 0) {
      addNewModelEntry()
    }
  },
  { deep: true, immediate: true },
)

watch(modelValueList, () => emit("update:modelValue", modelValueList.value), {
  deep: true,
})

watch(editIndex, focusFirstFocusableElementOfCurrentEditElement)
</script>

<template>
  <div class="w-full">
    <div
      v-for="(entry, index) in modelValueList"
      :key="index"
      ref="elementList"
      aria-label="Listen Eintrag"
      class="group border-b-1 border-gray-400"
    >
      <div
        v-if="index !== editIndex"
        :key="index"
        class="flex cursor-pointer items-center justify-between gap-8 py-8 group-first:pt-0"
        :class="{ '!border-none': noHorizontalSeparators }"
      >
        <component
          :is="summaryComponent"
          class="focus:outline-none focus-visible:outline-blue-800"
          :data="entry"
          tabindex="0"
          @click="setEditIndex(index)"
          @keypress.enter="setEditIndex(index)"
        />

        <div class="flex gap-8">
          <button
            aria-label="Eintrag bearbeiten"
            class="material-icons p-2 text-blue-800 outline-none outline-offset-2 hover:bg-blue-200 focus:outline-2 focus:outline-blue-800 active:bg-blue-500 active:outline-none"
            @click="setEditIndex(index)"
          >
            edit_note
          </button>

          <button
            aria-label="Eintrag löschen"
            class="material-icons p-2 text-blue-800 outline-none outline-offset-2 hover:bg-blue-200 focus:outline-2 focus:outline-blue-800 active:bg-blue-500 active:outline-none"
            @click="removeModelEntry(index)"
          >
            delete_outline
          </button>
        </div>
      </div>

      <component
        :is="editComponent"
        v-else
        v-model="modelValueList[index]"
        class="py-16 group-first:pt-0"
        @keypress.enter="setEditIndex(undefined)"
      />
    </div>

    <button
      v-if="!disableMultiEntry"
      aria-label="Weitere Angabe"
      class="add-button gap-0.5 mt-16 flex items-center whitespace-nowrap bg-blue-300 px-8 py-2 text-14 font-bold leading-18 text-blue-800 outline-none outline-0 outline-offset-4 outline-blue-800 hover:bg-blue-800 hover:text-white focus:outline-4"
      @click="addNewModelEntry"
    >
      <span class="material-icons text-14">add</span>
      Weitere Angabe
    </button>
  </div>
</template>
