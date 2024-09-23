<script lang="ts" setup>
import { ref, computed, watch, nextTick } from "vue"
import TextButton from "@/components/input/TextButton.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const keywordsLength = computed(
  () => store.documentUnit!.contentRelatedIndexing.keywords?.length,
)

const localKeywords = ref()

const keywords = computed({
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
  store.documentUnit!.contentRelatedIndexing.keywords = localKeywords.value
}
const cancelEdit = () => {
  // todo: switch to displaymode
}

const adjustTextareaHeight = (textarea: HTMLTextAreaElement | null) => {
  if (textarea) {
    textarea.style.height = "auto" // Reset height first to recalculate based on content
    textarea.style.height = `${textarea.scrollHeight}px ` // Set the height to match content
  }
}

// Watch the `keywords` value to adjust the height when content changes
watch(keywords, async () => {
  await nextTick() // Wait for the content update
  const textarea = document.querySelector("textarea")
  adjustTextareaHeight(textarea as HTMLTextAreaElement)
})
</script>

<template>
  <div>
    <h2 class="ds-label-01-bold mb-16">Schlagwörter</h2>

    <div class="flex flex-col gap-24">
      <div>
        <label class="ds-label-02-reg mb-4">Schlagwörter</label>
        <textarea
          id="keywords"
          v-model="keywords"
          class="ds-input h-auto resize-none overflow-hidden p-20"
          placeholder="Geben Sie jeden Wert in eigene Zeile ein"
          :rows="keywordsLength"
          @input="adjustTextareaHeight($event.target as HTMLTextAreaElement)"
        ></textarea>
      </div>
      <div class="flex w-full flex-row">
        <div class="flex gap-16">
          <TextButton
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
  </div>
</template>
