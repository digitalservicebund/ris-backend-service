<script lang="ts" setup>
import { ref, computed, watch, nextTick, onMounted } from "vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"

const props = defineProps<{
  label: string
  modelValue: string[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: string[]]
}>()

const editMode = ref(true)
const sortAlphabetically = ref(false)
const localModelValue = ref(props.modelValue ?? [])

const textAreaInput = computed({
  get: () => (props.modelValue ? props.modelValue.join("\n") : ""), // Join array with newlines for textarea
  set: (newValues: string) => {
    // Split the text by newline, trim each line, filter out empty lines, and set back into the array
    localModelValue.value = newValues
      .split("\n")
      .map((listitem) => listitem.trim())
      .filter((listitem) => listitem !== "")
  },
})

const addList = () => {
  // sort alphabetically if option set
  if (sortAlphabetically.value && localModelValue.value) {
    localModelValue.value = localModelValue.value.sort((a: string, b: string) =>
      a.localeCompare(b),
    )
  }
  emit("update:modelValue", [...new Set(localModelValue.value)] as string[]) //remove duplicates
  if (!!localModelValue.value?.length) editMode.value = false
  sortAlphabetically.value = false
}
const cancelEdit = () => {
  if (!!localModelValue.value?.length) editMode.value = false
}

const toggleEditMode = () => {
  editMode.value = !editMode.value
}

const adjustTextareaHeight = (textarea: HTMLTextAreaElement | null) => {
  if (textarea) {
    textarea.style.height = "auto" // Reset height first to recalculate based on content
    textarea.style.height = `${textarea.scrollHeight}px ` // Set the height to match content
  }
}

// Watch the `textAreaInput` value to adjust the height when content changes
watch(textAreaInput, async () => {
  await nextTick() // Wait for DOM render
  const textarea = document.querySelector("textarea")
  adjustTextareaHeight(textarea as HTMLTextAreaElement)
})

onMounted(() => {
  // When data, show display mode. When empty, show edit mode
  editMode.value = !props.modelValue.length
})
</script>

<template>
  <div>
    <!-- Edit mode -->
    <div v-if="editMode" class="flex flex-col gap-24">
      <div class="flex flex-col gap-8">
        <label id="list-input" class="ds-label-02-reg mb-4"> {{ label }}</label>
        <textarea
          id="list-input"
          v-model="textAreaInput"
          :aria-label="`${label} Input`"
          class="ds-input h-auto resize-none overflow-hidden p-20"
          placeholder="Geben Sie jeden Wert in eigene Zeile ein"
          :rows="modelValue.length"
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
            @click.stop="addList"
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
    <!-- Display mode -->
    <div v-else class="flex flex-col gap-16">
      <div class="flex flex-col gap-8">
        <span class="ds-label-02-reg mb-4">{{ label }}</span>
        <ul class="m-0 flex flex-row gap-8 p-0">
          <li
            v-for="(chip, i) in modelValue"
            :key="i"
            class="rounded-full bg-blue-300"
            data-testid="chip"
          >
            <span
              class="overflow-hidden text-ellipsis whitespace-nowrap px-8 py-6 text-18"
              data-testid="chip-value"
              >{{ chip }}
            </span>
          </li>
        </ul>
      </div>
      <TextButton
        :aria-label="`${label} bearbeiten`"
        button-type="tertiary"
        :label="`${label} bearbeiten`"
        size="small"
        @click.stop="toggleEditMode"
      />
    </div>
  </div>
</template>
