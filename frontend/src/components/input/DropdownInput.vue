<script lang="ts" setup>
import { computed } from "vue"
import { DropdownInputModelType, DropdownItem } from "@/components/input/types"

const props = defineProps<{
  items: DropdownItem[]
  modelValue?: DropdownInputModelType
  placeholder?: string
  readOnly?: boolean
  hasError?: boolean
  isSmall?: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [DropdownInputModelType | undefined]
}>()

const localModelValue = computed({
  get: () => props.modelValue ?? "",
  set: (value) => {
    // Emit only if value is in the dropdown items
    if (props.items.some((item) => item.value === value)) {
      emit("update:modelValue", value)
    }
  },
})

const conditionalClasses = computed(() => ({
  "ds-select-small": props.isSmall,
  "has-error": props.hasError,
  "ds-select-medium": !props.isSmall,
}))

const hasPlaceholder = computed(() =>
  Boolean(!props.modelValue && props.placeholder),
)

// Compute the display value for the dropdown, if modelValue is not in the options
const displayValue = computed(() => {
  const selectedItem = props.items.find(
    (item) => item.value === props.modelValue,
  )
  return selectedItem ? selectedItem.label : props.modelValue // Fallback to raw value if not found
})
</script>

<template>
  <!-- Label should come from the surrounding context, e.g. InputField component -->
  <!-- eslint-disable vuejs-accessibility/form-control-has-label -->
  <select
    v-model="localModelValue"
    class="ds-select"
    :class="conditionalClasses"
    :data-placeholder="hasPlaceholder ? true : undefined"
    :disabled="readOnly"
    tabindex="0"
  >
    <option v-if="placeholder && !localModelValue" disabled value="">
      {{ placeholder }}
    </option>
    <option v-for="item in items" :key="item.value" :value="item.value">
      {{ item.label }}
    </option>
    <!-- Special case: Display the model value, also if it's not in the options (raw value) -->
    <option
      v-if="modelValue && !items.some((item) => item.value === modelValue)"
      :value="modelValue"
    >
      {{ displayValue }}
    </option>
  </select>
</template>
