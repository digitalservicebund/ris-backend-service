<script lang="ts" setup>
import {
  DropdownInputModelType,
  DropdownItem,
} from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  items: DropdownItem[]
  modelValue?: DropdownInputModelType
  value?: DropdownInputModelType
  ariaLabel: string
  placeholder?: string
}

interface Emits {
  (event: "update:modelValue", value?: DropdownInputModelType): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emits = defineEmits<Emits>()

const { inputValue } = useInputModel<string, Props, Emits>(props, emits)

</script>

<template>
  <select
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="ds-select outline-none w-full"
    tabindex="0"
  >
    <option
        v-if="placeholder"
        disabled
        value=""
      >{{ placeholder }}</option>
    <option
      v-for="(item, index) in items"
      :key="index"
      :value="item.value"
    >
      {{ item.label }}
    </option>
  </select>
</template>
