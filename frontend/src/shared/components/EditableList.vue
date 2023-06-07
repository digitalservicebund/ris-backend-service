<script lang="ts" setup>
import type { Component } from "vue"
import { computed, nextTick, onMounted, ref, useAttrs, watch } from "vue"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"

interface Props {
  editComponent: Component
  summaryComponent?: Component
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  modelValue?: any[]
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  defaultValue: any
  disableMultiEntry?: boolean
}

interface Emits {
  (event: "update:modelValue", value: undefined[]): void
  (event: "deleteLastEntry"): void
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  modelValue: () => [],
  disableMultiEntry: false,
})

const emit = defineEmits<Emits>()
const attributes = useAttrs()

const modelValueList = ref<undefined[]>([])
const elementList = ref<HTMLElement[]>([])
const editIndex = ref<number | undefined>(undefined)

const currentEditElement = computed(() =>
  editIndex.value !== undefined ? elementList.value[editIndex.value] : undefined
)

function setEditIndex(index: number | undefined) {
  editIndex.value = index
}

function addNewModelEntry() {
  const { defaultValue } = props
  const newEntry =
    typeof defaultValue === "object" ? { ...defaultValue } : defaultValue
  modelValueList.value.push(newEntry)
  editIndex.value = modelValueList.value.length - 1
}

function removeModelEntry(index: number) {
  modelValueList.value.splice(index, 1)

  if (editIndex.value !== undefined && index < editIndex.value) {
    editIndex.value -= 1
  }
}

async function focusFirstFocusableElementOfCurrentEditElement() {
  await nextTick()

  if (currentEditElement.value) {
    const firstFocusableElement = currentEditElement.value.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )[0] as HTMLElement
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
  { immediate: true, deep: true }
)

watch(
  modelValueList,
  () => {
    if (modelValueList.value.length == 0) {
      addNewModelEntry()
    }
  },
  { deep: true, immediate: true }
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
      class="border-b-1 border-gray-400 group"
    >
      <div
        v-if="index !== editIndex"
        :key="index"
        class="cursor-pointer flex gap-8 group-first:pt-0 items-center justify-between py-8"
      >
        <component
          :is="summaryComponent"
          class="focus-visible:outline-blue-800 focus:outline-none"
          :data="entry"
          tabindex="0"
          @click="setEditIndex(index)"
          @keypress.enter="setEditIndex(index)"
        />

        <div class="flex gap-8">
          <button
            aria-label="Eintrag bearbeiten"
            class="active:bg-blue-500 active:outline-none focus:outline-2 focus:outline-blue-800 hover:bg-blue-200 material-icons outline-none outline-offset-2 p-2 text-blue-800"
            @click="setEditIndex(index)"
          >
            edit_note
          </button>

          <button
            aria-label="Eintrag löschen"
            class="active:bg-blue-500 active:outline-none focus:outline-2 focus:outline-blue-800 hover:bg-blue-200 material-icons outline-none outline-offset-2 p-2 text-blue-800"
            @click="removeModelEntry(index)"
          >
            delete_outline
          </button>
        </div>
      </div>

      <component
        :is="editComponent"
        v-else
        v-bind="attributes"
        v-model="modelValueList[index]"
        class="group-first:pt-0 py-16"
        @keypress.enter="setEditIndex(undefined)"
      />
    </div>

    <button
      v-if="!disableMultiEntry"
      aria-label="Weitere Angabe"
      class="add-button bg-blue-300 flex focus:outline-4 font-bold gap-0.5 hover:bg-blue-800 hover:text-white items-center leading-18 mt-16 outline-0 outline-blue-800 outline-none outline-offset-4 px-8 py-2 text-14 text-blue-800 whitespace-nowrap"
      @click="addNewModelEntry"
    >
      <span class="material-icons text-14">add</span>
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
