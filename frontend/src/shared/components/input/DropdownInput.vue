<script lang="ts" setup>
import { computed } from "vue"
import {
  DropdownInputModelType,
  DropdownItem,
} from "@/shared/components/input/types"

const props = defineProps<{
  items: DropdownItem[]
  modelValue?: DropdownInputModelType
  placeholder?: string
  clearable?: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [DropdownInputModelType | undefined]
}>()

const localModelValue = computed({
  get: () => props.modelValue ?? "",
  set: (value) => {
    emit("update:modelValue", value)
  },
})

const hasPlaceholder = computed(() =>
  Boolean(!props.modelValue && props.placeholder),
)
</script>

<template>
  <!-- Label should come from the surrounding context, e.g. InputField component -->
  <!-- eslint-disable vuejs-accessibility/form-control-has-label -->
  <select
    v-model="localModelValue"
    class="ds-select data-[placeholder]:font-font-family-serif data-[placeholder]:italic data-[placeholder]:text-gray-800 data-[placeholder]:text-opacity-25"
    :data-placeholder="hasPlaceholder ? true : undefined"
    tabindex="0"
  >
    <option
      v-if="!clearable && placeholder && !localModelValue"
      disabled
      value=""
    >
      {{ placeholder }}
    </option>
    <option v-if="clearable && placeholder" value="">
      {{ placeholder }}
    </option>
    <option v-for="item in items" :key="item.value" :value="item.value">
      {{ item.label }}
    </option>
  </select>
</template>
