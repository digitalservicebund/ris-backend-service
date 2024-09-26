<script lang="ts" setup>
import { ref, computed, watch, nextTick } from "vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"

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
const sortAlphabetically = computed({
  get: () => props.sortAlphabetically,

  set: (value) => {
    emit("toggleSorting", value)
  },
})

const cancelEdit = () => {
  if (!!localModelValue.value?.length) emit("toggle")
}

const adjustTextareaHeight = (textarea: HTMLTextAreaElement | null) => {
  if (textarea) {
    textarea.style.height = "auto" // Reset height first to recalculate based on content
    textarea.style.height = `${textarea.scrollHeight}px ` // Set the height to match content
  }
}

// Watch the `textAreaInput` value to adjust the height when content changes
watch(
  localModelValue,
  async () => {
    await nextTick() // Wait for DOM render
    const textarea = document.querySelector("textarea")
    adjustTextareaHeight(textarea as HTMLTextAreaElement)
  },
  { immediate: true },
)
</script>

<template>
  <div>
    <div class="flex flex-col gap-24">
      <div class="flex flex-col gap-8">
        <label id="list-input" class="ds-label-02-reg mb-4"> {{ label }}</label>
        <textarea
          id="list-input"
          v-model="localModelValue"
          :aria-label="`${label} Input`"
          class="ds-input h-auto resize-none overflow-hidden p-20"
          placeholder="Geben Sie jeden Wert in eigene Zeile ein"
          :rows="listItemCount"
          @input="adjustTextareaHeight($event.target as HTMLTextAreaElement)"
        ></textarea>
      </div>
      <InputField
        id="sortAlphabetically"
        label="Alphabetisch sortieren"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          id="sortAlphabetically"
          v-model="sortAlphabetically"
          aria-label="Alphabetisch sortieren"
          class="ds-checkbox-mini bg-white"
        />
      </InputField>
      <div class="flex w-full flex-row">
        <div class="flex gap-16">
          <TextButton
            :aria-label="`${label} übernehmen`"
            button-type="primary"
            label="Übernehmen"
            size="small"
            @click.stop="emit('update:modelValue', localModelValue)"
          />
          <TextButton
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="cancelEdit"
          />
        </div>
      </div>
    </div>
  </div>
</template>
