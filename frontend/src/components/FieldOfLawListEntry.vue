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
  <div class="mt-20 flex">
    <div class="flex grow flex-col">
      <div class="flex">
        <div class="ds-label-02-reg flex pt-8 text-blue-800">
          <span
            :aria-label="
              fieldOfLaw.identifier +
              ' ' +
              fieldOfLaw.text +
              ' im Sachgebietsbaum anzeigen'
            "
            class="link mr-12 w-44 whitespace-nowrap"
            role="button"
            tabindex="0"
            @click="emit('node-clicked')"
            @keyup.enter="emit('node-clicked')"
          >
            {{ fieldOfLaw.identifier }}
          </span>
          <span class="ml-112 text-black">
            <TokenizeText
              :keywords="fieldOfLaw.linkedFields ?? []"
              :text="fieldOfLaw.text"
              @link-token:clicked="handleTokenClick"
            />
          </span>
        </div>
      </div>
    </div>
    <div v-if="props.showBin">
      <button
        :aria-label="
          fieldOfLaw.identifier + ' ' + fieldOfLaw.text + ' aus Liste entfernen'
        "
        class="text-blue-800"
        @click="emit('remove-from-list')"
      >
        <IconDelete />
      </button>
    </div>
  </div>
  <hr class="mt-8 w-full border-blue-500" />
</template>

<style lang="scss" scoped>
.link {
  cursor: pointer;
  text-decoration: underline;

  &:active {
    text-decoration-thickness: 4px;
  }

  &:focus {
    border: 4px solid #004b76;
  }
}
</style>
