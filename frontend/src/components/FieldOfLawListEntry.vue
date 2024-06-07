<script lang="ts" setup>
import FlexContainer from "@/components/FlexContainer.vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconDelete from "~icons/ic/outline-delete"

interface Props {
  fieldOfLaw: FieldOfLaw
  showBin?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:select": [node: FieldOfLaw]
  "linked-field:select": [node: FieldOfLaw]
}>()
</script>

<template>
  <FlexContainer
    class="ds-label-02-reg border-b-1 border-blue-500 py-20 text-blue-800 first:border-t-1"
    flex-direction="flex-row"
    justify-content="justify-start"
  >
    <FlexContainer flex-direction="flex-col">
      <button
        :aria-label="
          fieldOfLaw.identifier +
          ' ' +
          fieldOfLaw.text +
          ' im Sachgebietsbaum anzeigen'
        "
        class="mr-12 whitespace-nowrap text-left underline"
        tabindex="0"
        @click="emit('node:select', fieldOfLaw)"
        @keyup.enter="emit('node:select', fieldOfLaw)"
      >
        {{ fieldOfLaw.identifier }}
      </button>
      <button class="text-left text-black">
        <TokenizeText
          :keywords="fieldOfLaw.linkedFields ?? []"
          :text="fieldOfLaw.text"
          @linked-field:select="emit('linked-field:select', $event)"
        />
      </button>
    </FlexContainer>

    <FlexContainer
      v-if="props.showBin"
      class="flex-grow"
      justify-content="justify-end"
    >
      <button
        :aria-label="
          fieldOfLaw.identifier + ' ' + fieldOfLaw.text + ' aus Liste entfernen'
        "
        class="align-middle text-blue-800"
        @click="emit('node:remove', fieldOfLaw)"
      >
        <IconDelete />
      </button>
    </FlexContainer>
  </FlexContainer>
</template>
