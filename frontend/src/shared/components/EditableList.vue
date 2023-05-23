<script lang="ts" setup>
import { computed, nextTick, ref, useAttrs, watch, onMounted } from "vue"
import type { Component } from "vue"
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
    >
      <div
        v-if="index !== editIndex"
        :key="index"
        class="border-b-1 border-b-blue-500 cursor-pointer flex justify-between py-10"
      >
        <component
          :is="summaryComponent"
          :data="entry"
          tabindex="0"
          @click="setEditIndex(index)"
          @keypress.enter="setEditIndex(index)"
        />

        <div class="flex">
          <button
            aria-label="Eintrag bearbeiten"
            class="icon material-icons"
            @click="setEditIndex(index)"
          >
            edit_note
          </button>

          <button
            aria-label="Eintrag löschen"
            class="icon material-icons"
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
        class="mt-16"
        @keypress.enter="setEditIndex(undefined)"
      />
    </div>

    <button
      v-if="!disableMultiEntry"
      aria-label="Weitere Angabe"
      class="add-button bg-blue-300 focus:outline-4 font-bold gap-0.5 hover:bg-blue-800 hover:text-white inline-flex items-center leading-18 mt-16 outline-0 outline-blue-800 outline-none outline-offset-4 pr-[0.25rem] py-[0.125rem] text-14 text-blue-800 whitespace-nowrap"
      @click="addNewModelEntry"
    >
      <span class="material-icons text-14">add</span>
      Weitere Angabe
    </button>
  </div>
</template>

<style lang="scss" scoped>
.icon {
  padding: 3px 2px;
  color: #004b76;
  outline: none;

  &:hover {
    background-color: #ecf1f4;
  }

  &:focus {
    padding: 1px 0;
    border: 2px solid #004b76;
  }

  &:active {
    border: none !important;
    background: #b3c9d6;
    outline: none;
  }
}

.add-button {
  &:focus:not(:focus-visible) {
    @apply outline-transparent;
  }
}
</style>
