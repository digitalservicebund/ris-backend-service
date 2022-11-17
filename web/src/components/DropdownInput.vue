<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue"
import { Court } from "@/domain/documentUnit"
import type { DropdownItem } from "@/domain/types"
import { DropdownInputModelType, LookupTableEndpoint } from "@/domain/types"
import lookupTableService from "@/services/lookupTableService"

interface Props {
  id: string
  value?: DropdownInputModelType // TODO do we need this?
  modelValue?: DropdownInputModelType
  ariaLabel: string
  placeholder?: string
  dropdownItems?: DropdownItem[]
  endpoint?: LookupTableEndpoint
  isCombobox?: boolean
  preselectedValue?: string
}

interface Emits {
  (event: "update:modelValue", value: DropdownInputModelType | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref<DropdownInputModelType>()
const inputText = ref<string>()

watch(
  props,
  () => {
    inputValue.value = props.modelValue ?? props.value
    checkValue()
  },
  {
    immediate: true,
  }
)

watch(inputValue, () => {
  emit("update:modelValue", inputValue.value)
  checkValue()
})

function checkValue() {
  // TODO better solution to check for court type
  if (typeof inputValue.value === "object") {
    const court = inputValue.value as Court
    inputText.value = court.label
  } else {
    inputText.value = inputValue.value as string
  }
}

const isShowDropdown = ref(false)
const items = ref(props.dropdownItems ?? [])
const currentItems = ref<DropdownItem[]>([]) // the items currently displayed in the dropdown
const itemRefs = ref([])
const filter = ref<string>()

const toggleDropdown = () => {
  isShowDropdown.value = !isShowDropdown.value
  if (isShowDropdown.value) {
    updateCurrentItems()
  }
}

const clearSelection = () => {
  emit("update:modelValue", undefined)
  filter.value = ""
  updateCurrentItems()
}

const setChosenItem = (value: DropdownInputModelType) => {
  emit("update:modelValue", value)
  filter.value = ""
  isShowDropdown.value = false
}

const keyup = (index: number) => {
  const prev = itemRefs.value[index - 1] as HTMLElement
  if (prev) prev.focus()
}

const keydown = (index: number) => {
  const next = itemRefs.value[index + 1] as HTMLElement
  if (next) next.focus()
}

const onTextChange = () => {
  isShowDropdown.value = true
  filter.value = inputText.value
  updateCurrentItems()
}

const updateCurrentItems = () => {
  if (!!props.endpoint) {
    lookupTableService
      .fetch(props.endpoint, filter.value)
      .then((dropdownItems: DropdownItem[]) => {
        currentItems.value = dropdownItems
        insertItemIfEmpty()
      })
  } else {
    currentItems.value = items.value.filter((item) =>
      item.text.includes(!!filter.value ? filter.value : "")
    )
    insertItemIfEmpty()
  }
}

const insertItemIfEmpty = () => {
  if (currentItems.value.length === 0) {
    currentItems.value = [{ text: "Kein passender Eintrag", value: "" }]
  }
}

const closeDropDownWhenClickOutSide = (event: MouseEvent) => {
  const dropdown = document.querySelector(`#${props.id}.dropdown-container`)
  if (dropdown == null) return
  if (
    (event.target as HTMLElement) === dropdown ||
    event.composedPath().includes(dropdown)
  )
    return
  isShowDropdown.value = false
}

const selectAllText = () => {
  const inputField = document.querySelector(
    `input#${props.id}`
  ) as HTMLInputElement
  if (!!props.modelValue) inputField.select()
}

const closeDropdown = () => {
  isShowDropdown.value = false
}

onMounted(() => {
  if (props.preselectedValue) inputValue.value = props.preselectedValue
  window.addEventListener("click", closeDropDownWhenClickOutSide)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <div :id="id" class="dropdown-container" @keydown.esc="closeDropdown">
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="bg-white input-container">
        <input
          :id="id"
          v-model="inputText"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="text-input"
          :placeholder="placeholder"
          :readonly="!props.isCombobox"
          tabindex="0"
          @click="selectAllText"
          @input="onTextChange"
        />
        <button
          v-if="isCombobox"
          class="input-close-icon"
          tabindex="0"
          @click="clearSelection"
          @keydown.enter="clearSelection"
        >
          <span class="icon material-icons pr-[1.5rem] text-blue-800">
            close
          </span>
        </button>
        <button
          class="input-expand-icon"
          tabindex="0"
          @click="toggleDropdown"
          @keydown.enter="toggleDropdown"
        >
          <span
            v-if="!isShowDropdown"
            class="icon material-icons text-blue-800"
          >
            expand_more
          </span>
          <span v-else class="icon material-icons"> expand_less </span>
        </button>
      </div>
    </div>
    <div
      v-if="isShowDropdown"
      class="dropdown-container__dropdown-items"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in currentItems"
        :key="index"
        ref="itemRefs"
        class="dropdown-container__dropdown-item"
        tabindex="0"
        @click="setChosenItem(item.value)"
        @keypress.enter="setChosenItem(item.value)"
        @keyup.down="keydown(index)"
        @keyup.up="keyup(index)"
      >
        <span> {{ item.text }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dropdown-container {
  position: relative;
  display: inline-block;
  width: 100%;
  user-select: none;

  &__open-dropdown {
    @apply border-2 border-solid border-blue-900;

    .input-container {
      @apply hover:shadow-hover hover:shadow-blue-900 focus:shadow-focus focus:shadow-blue-900;

      display: flex;
      flex: row nowrap;
      justify-content: space-between;
      padding: 17px 24px;

      .text-input {
        width: 100%;

        &:focus {
          outline: none;
        }
      }

      .input-close-icon,
      .input-expand-icon {
        height: 5px;
        margin-top: 3px;
      }
    }
  }

  &__dropdown-items {
    /** Always show on top after textbox and width equal to textbox */
    position: absolute;
    z-index: 1;
    top: 100%;
    right: 0;
    left: 0;
    display: flex;
    max-height: 300px;
    flex-direction: column;
    filter: drop-shadow(0 1px 3px rgb(0 0 0 / 25%));
    overflow-y: scroll;
    scrollbar-width: none;
  }

  &__dropdown-item {
    @apply bg-white border-b-1 border-b-gray-400 cursor-pointer py-[1.063rem] px-[1.5rem];

    &:last-of-type {
      @apply border-b-0;
    }

    &:hover {
      @apply bg-blue-200;
    }

    &:focus {
      @apply bg-blue-200;
    }
  }
}
</style>
