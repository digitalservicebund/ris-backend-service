<script setup lang="ts" generic="T extends object">
import AutoComplete, {
  AutoCompleteChangeEvent,
  AutoCompleteCompleteEvent,
  AutoCompleteOptionSelectEvent,
} from "primevue/autocomplete"
import ProgressSpinner from "primevue/progressspinner"
import { computed, nextTick, Ref, ref, watch } from "vue"
import { ComboboxItem } from "@/components/input/types"
import { ComboboxItemService } from "@/services/comboboxItemService"
import IcOutlineClear from "~icons/ic/outline-clear?height=16"
import IconChevron from "~icons/mdi/chevron-down"

const props = withDefaults(
  defineProps<{
    id: string
    itemService: ComboboxItemService<T>
    modelValue: T | undefined
    ariaLabel: string
    placeholder?: string
    manualEntry?: boolean
    noClear?: boolean
    hasError?: boolean
    readOnly?: boolean
    comparisonFunction?: (a?: T, b?: T) => boolean
  }>(),
  {
    placeholder: undefined,
    manualEntry: false,
    noClear: false,
    hasError: false,
    readOnly: false,
    comparisonFunction: (a, b) => {
      if (a == null || b == null) {
        return false
      }

      if ("id" in a && "id" in b) {
        return a.id === b.id
      }

      if ("uuid" in a && "uuid" in b) {
        return a.uuid === b.uuid
      }

      if ("identifier" in a && "identifier" in b) {
        return a.identifier === b.identifier
      }

      if ("label" in a && "label" in b) {
        return a.label === b.label
      }

      return a === b
    },
  },
)

const emit = defineEmits<{
  "update:modelValue": [value?: T]
  focus: [void]
}>()

const filter = ref<string>()

const conditionalClasses = computed(() => ({
  "!shadow-red-900 !bg-red-200": props.hasError,
  "!shadow-none !bg-blue-300": props.readOnly,
}))

const {
  useFetch: { data: existingItems, execute: fetchItems, isFetching },
  format,
} = props.itemService(filter)

type SelectionItem = ComboboxItem<T> | { manualEntry: true; label: string }

const selectionItems = computed<SelectionItem[]>(() => {
  const formatedExistingItems =
    existingItems.value?.map((item) => format(item)) ?? []

  const exactMatchFound = formatedExistingItems.find(
    (item) => item.label === filter.value?.trim(),
  )

  if (props.manualEntry && filter.value && !exactMatchFound) {
    return [
      ...formatedExistingItems,
      {
        manualEntry: true,
        label: filter.value,
      },
    ]
  }

  return formatedExistingItems
})

const handleComplete = async (e: AutoCompleteCompleteEvent) => {
  filter.value = e.query
  await fetchItems()
}

const handleOptionSelect = (e: AutoCompleteOptionSelectEvent) => {
  const value: SelectionItem = e.value
  if ("manualEntry" in value) {
    emit("update:modelValue", {
      label: value.label,
    } as T)
  } else {
    emit("update:modelValue", value.value)
  }
}

const handleChange = async (e: AutoCompleteChangeEvent) => {
  const value: SelectionItem | string | undefined = e.value

  // We only want to handle some cases where the value is empty. See also the comment in updateModelValue
  if (
    ((value == undefined || value === "") &&
      e.originalEvent instanceof PointerEvent) || // clear via the clear button
    value === "" // manually cleared the input by pressing backspace
  ) {
    emit("update:modelValue", undefined)
    // we also want to run a new query when the filter is cleared
    filter.value = undefined
    await fetchItems()
  }
}

// We create an internal value so we do not refresh the value if the model-value changes, but it's still the same value
// according to the comparison function. This way the input does not reset during typing. The model-value changes as a
// new object is created after a autosave and the old and new value are not strictly equal.
const internalValue: Ref<T | undefined> = ref(undefined)
watch(
  () => props.modelValue,
  (newValue, oldValue) => {
    if (
      !props.comparisonFunction(newValue, oldValue) &&
      !(newValue == undefined && oldValue == undefined)
    ) {
      internalValue.value = newValue
    }
  },
  { immediate: true },
)

const updateModelValue = async (e: T | undefined) => {
  if (e == null) {
    // We want to keep showing the current value when the component is no longer focused with an invalid input
    // As force-selection is active the autocomplete automatically resets the model-value when the focus is lost to
    // null. It also clears the text-input. We therefore need to ensure that the text-input is again showing the current
    // modelValue of our component. For this we need to update the internalValue. The internalValue already has the
    // current modelValue as its value. Therefore, just setting it would not trigger an update. So we need to set it to
    // undefined for 1 tick and then set the value to the modelValue again. This then tricks the autocomplete component
    // to update the text-input.
    // It is still possible to manually clear the autocomplete as in that case also our modelValue gets set to
    // undefined as well.
    internalValue.value = undefined
    await nextTick()
    internalValue.value = props.modelValue
  }
}

function toLabel(option: ComboboxItem<T> | T) {
  // the options passed via suggestions are already formatted and therefore have a label
  if ("label" in option && !("uuid" in option)) {
    return option.label
  }

  // but for some reason also the current value gets passed to this method and it is not yet formated
  return format(option as T).label
}
</script>

<template>
  <AutoComplete
    :id="props.id"
    append-to="self"
    :aria-label="props.ariaLabel"
    auto-highlight
    auto-option-focus
    class="relative w-full"
    :class="conditionalClasses"
    complete-on-focus
    :default-value="internalValue"
    :disabled="readOnly"
    dropdown-mode="current"
    fluid
    force-selection
    :loading="isFetching"
    :option-label="toLabel"
    :placeholder="props.placeholder"
    :show-clear="!props.noClear"
    :suggestions="selectionItems"
    @change="handleChange"
    @complete="handleComplete"
    @focus="$emit('focus')"
    @option-select="handleOptionSelect"
    @update:model-value="updateModelValue"
  >
    <template #loader>
      <ProgressSpinner class="absolute inset-y-0 right-8 my-auto mr-1" />
    </template>
    <template #empty>Kein passender Eintrag</template>
    <template #clearicon="{ clearCallback }">
      <button
        aria-label="Entfernen"
        class="p-8 hover:bg-blue-100 hover:text-blue-800 focus-visible:bg-blue-800 focus-visible:text-white focus-visible:outline-none"
        @click="clearCallback"
      >
        <IcOutlineClear />
      </button>
    </template>
    <template #dropdown="{ toggleCallback }">
      <button
        aria-haspopup="listbox"
        aria-label="Vorschläge anzeigen"
        class="p-8 hover:bg-blue-100 hover:text-blue-800 focus-visible:bg-blue-800 focus-visible:text-white focus-visible:outline-none"
        @click="toggleCallback"
      >
        <IconChevron />
      </button>
    </template>
    <template #option="slotProps: { option: SelectionItem }">
      <div
        v-if="'manualEntry' in slotProps.option"
        class="flex min-h-48 flex-col justify-start gap-2 border-l-4 border-transparent px-12 py-10"
      >
        <span class="ris-label1-regular font-bold">
          {{ slotProps.option?.label }} neu erstellen
        </span>
      </div>
      <div
        v-else
        class="flex min-h-48 flex-col justify-start gap-2 border-l-4 border-transparent px-12 py-10 data-[variant=active]:-ml-4 data-[variant=active]:border-blue-800 data-[variant=active]:bg-blue-200"
        :data-variant="
          slotProps.option.value &&
          props.comparisonFunction(slotProps.option.value, internalValue)
            ? 'active'
            : ''
        "
      >
        <span class="ris-label1-regular">
          {{ slotProps.option?.label }}
        </span>

        <div class="flex flex-row">
          <span
            v-if="slotProps.option?.additionalInformation"
            aria-label="additional-dropdown-info"
            class="ris-label2-regular flex-grow-1 text-gray-700"
          >
            {{ slotProps.option?.additionalInformation }}
          </span>
          <span
            v-if="slotProps.option?.sideInformation"
            id="dropDownSideInformation"
            class="ris-label2-regular text-gray-700"
          >
            {{ slotProps.option?.sideInformation }}
          </span>
        </div>
      </div>
    </template>
  </AutoComplete>
</template>
