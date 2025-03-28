<script lang="ts" setup>
import Button from "primevue/button"
import Checkbox from "primevue/checkbox"
import { computed, nextTick, ref, watch } from "vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"

const props = defineProps<{
  label: string
  modelValue: string
  sortAlphabetically: boolean
  listItemCount: number
}>()

const emit = defineEmits<{
  "update:modelValue": [value: string]
  toggle: []
  toggleSorting: [value: boolean]
}>()

const localModelValue = ref(props.modelValue)

/**
 * Computed property to manage sorting alphabetically.
 * - Get: Retrieves the value of `sortAlphabetically` from the list input.
 * - Set: Emits the `toggleSorting` event when the checkbox is toggled.
 * @returns {boolean} The current state of the `sortAlphabetically` option.
 */
const sortAlphabetically = computed({
  get: () => props.sortAlphabetically,
  set: (value) => {
    emit("toggleSorting", value)
  },
})

/**
 * Cancels the edit mode and toggles back to the display mode, if there is content.
 * When no content, this resets the list input to show the category wrapper.
 */
const cancelEdit = () => {
  emit("toggle")
}

/**
 * Adjusts the height of the textarea dynamically based on its content.
 * - Resets the height to `auto` and then sets it based on the content's scroll height.
 * @param {HTMLTextAreaElement | null} textarea - The textarea element whose height is to be adjusted.
 */
const adjustTextareaHeight = (textarea: HTMLTextAreaElement | null) => {
  if (textarea) {
    textarea.style.height = "auto"
    textarea.style.height = `${textarea.scrollHeight}px `
  }
}

/**
 * Watches the `localModelValue` to adjust the textarea height when the content changes.
 * - Uses `nextTick` to wait for the DOM to be rendered before adjusting the height.
 */
watch(
  localModelValue,
  async () => {
    await nextTick() // Wait for DOM render
    const textarea = document.querySelector("textarea")
    adjustTextareaHeight(textarea as HTMLTextAreaElement)
  },
  { immediate: true },
)

watch(
  () => props.modelValue,
  (newValue) => {
    localModelValue.value = newValue
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-col gap-4">
      <label id="list-input" class="ris-label2-regular" for="list-input-id">
        {{ label }}</label
      >
      <textarea
        id="list-input-id"
        v-model="localModelValue"
        :aria-label="`${label} Input`"
        class="ds-input h-auto resize-none overflow-hidden p-20"
        :data-testid="`${label}_ListInputEdit`"
        placeholder="Geben Sie jeden Wert in eine eigene Zeile ein"
        :rows="listItemCount"
        @input="adjustTextareaHeight($event.target as HTMLTextAreaElement)"
      ></textarea>
    </div>
    <InputField
      :id="`sortAlphabetically_${label}`"
      label="Alphabetisch sortieren"
      :label-position="LabelPosition.RIGHT"
    >
      <Checkbox
        :id="`sortAlphabetically_${label}`"
        v-model="sortAlphabetically"
        aria-label="Alphabetisch sortieren"
        binary
      />
    </InputField>
    <div class="flex w-full flex-row">
      <div class="flex gap-16">
        <Button
          :aria-label="`${label} übernehmen`"
          label="Übernehmen"
          size="small"
          @click.stop="emit('update:modelValue', localModelValue)"
        ></Button>
        <Button
          aria-label="Abbrechen"
          label="Abbrechen"
          size="small"
          text
          @click.stop="cancelEdit"
        ></Button>
      </div>
    </div>
  </div>
</template>
