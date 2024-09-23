<script lang="ts" setup>
import { ref, computed, watch, nextTick } from "vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const keywordsLength = computed(
  () => store.documentUnit!.contentRelatedIndexing.keywords?.length,
)
const editMode = ref(true)
const sortAlphabetically = ref(false)

const localKeywords = ref(store.documentUnit!.contentRelatedIndexing.keywords)

const keywordsString = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.keywords?.join("\n"), // Join array with newlines for textarea
  set: (newValues: string) => {
    // Split the text by newline, trim each line, filter out empty lines, and set back into the array
    localKeywords.value = newValues
      .split("\n")
      .map((keyword) => keyword.trim())
      .filter((keyword) => keyword !== "")
  },
})

const addKeywords = () => {
  if (sortAlphabetically.value && localKeywords.value) {
    localKeywords.value = localKeywords.value.sort((a: string, b: string) =>
      a.localeCompare(b),
    )
  }
  store.documentUnit!.contentRelatedIndexing.keywords = [
    ...new Set(localKeywords.value),
  ] as string[] //remove duplicates
  if (!!localKeywords.value?.length) editMode.value = false
  sortAlphabetically.value = false
}
const cancelEdit = () => {
  if (!!localKeywords.value?.length) editMode.value = false
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

// Watch the `keywords` value to adjust the height when content changes
watch(
  keywordsString,
  async () => {
    await nextTick() // Wait for the content update
    const textarea = document.querySelector("textarea")
    adjustTextareaHeight(textarea as HTMLTextAreaElement)
    editMode.value = !keywordsLength.value
  },
  { immediate: true },
)
</script>

<template>
  <div>
    <h2 class="ds-label-01-bold mb-16">Schlagwörter</h2>
    <!-- Edit mode -->
    <div v-if="editMode" class="flex flex-col gap-24">
      <div class="flex flex-col gap-8">
        <label class="ds-label-02-reg mb-4">Schlagwörter</label>
        <textarea
          id="keywords"
          v-model="keywordsString"
          aria-label="Schlagwörter Input"
          class="ds-input h-auto resize-none overflow-hidden p-20"
          placeholder="Geben Sie jeden Wert in eigene Zeile ein"
          :rows="keywordsLength"
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
            aria-label="Schlagwörter übernehmen"
            button-type="primary"
            label="Übernehmen"
            size="small"
            @click.stop="addKeywords"
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
        <label class="ds-label-02-reg">Schlagwörter</label>
        <ul class="m-0 flex flex-row gap-8 p-0">
          <li
            v-for="(chip, i) in store.documentUnit!.contentRelatedIndexing
              .keywords"
            :key="i"
            class="rounded-full bg-blue-300"
            data-testid="chip"
            tabindex="0"
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
        aria-label="Schlagwörter bearbeiten"
        button-type="tertiary"
        label="Schlagwörter bearbeiten"
        size="small"
        @click.stop="toggleEditMode"
      />
    </div>
  </div>
</template>
