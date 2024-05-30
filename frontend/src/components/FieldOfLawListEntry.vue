<script lang="ts" setup>
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import IconDelete from "~icons/ic/outline-delete"

interface Props {
  fieldOfLaw: FieldOfLawNode
  showBin?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "remove-from-list": []
  "node-clicked": []
  "linkedField:clicked": [identifier: string]
}>()

function handleTokenClick(tokenContent: string) {
  emit("linkedField:clicked", tokenContent)
}
</script>

<template>
  <div
    class="flex w-full grow items-center border-b-1 border-blue-500 py-20 first:border-t-1"
  >
    <div class="ds-label-02-reg flex flex w-full flex-col text-blue-800">
      <button
        :aria-label="
          fieldOfLaw.identifier +
          ' ' +
          fieldOfLaw.text +
          ' im Sachgebietsbaum anzeigen'
        "
        class="mr-12 w-44 whitespace-nowrap underline"
        tabindex="0"
        @click="emit('node-clicked')"
        @keyup.enter="emit('node-clicked')"
      >
        {{ fieldOfLaw.identifier }}
      </button>
      <button class="text-left text-black">
        <TokenizeText
          :keywords="fieldOfLaw.linkedFields ?? []"
          :text="fieldOfLaw.text"
          @link-token:clicked="handleTokenClick"
        />
      </button>
    </div>
    <div v-if="props.showBin">
      <button
        :aria-label="
          fieldOfLaw.identifier + ' ' + fieldOfLaw.text + ' aus Liste entfernen'
        "
        class="align-middle text-blue-800"
        @click="emit('remove-from-list')"
      >
        <IconDelete />
      </button>
    </div>
  </div>
</template>
