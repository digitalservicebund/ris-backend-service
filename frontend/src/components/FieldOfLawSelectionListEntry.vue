<script lang="ts" setup>
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{ fieldOfLaw: FieldOfLawNode }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
  (event: "node-clicked"): void
  (event: "linkedField:clicked", identifier: string): void
}>()

function handleTokenClick(tokenContent: string) {
  emit("linkedField:clicked", tokenContent)
}
</script>

<template>
  <div class="flex">
    <div class="flex flex-col grow">
      <div class="flex">
        <div class="label-02-reg pt-8 text-blue-800">
          <span
            :aria-label="
              fieldOfLaw.identifier +
              ' ' +
              fieldOfLaw.text +
              ' im Sachgebietsbaum anzeigen'
            "
            class="link"
            @click="emit('node-clicked')"
            @keyup.enter="emit('node-clicked')"
          >
            {{ props.fieldOfLaw.identifier }}
          </span>
        </div>
      </div>
      <div class="grow label-03-reg pb-16 pt-4 text-blue-800">
        <TokenizeText
          :keywords="props.fieldOfLaw.linkedFields ?? []"
          :text="props.fieldOfLaw.text"
          @link-token:clicked="handleTokenClick"
        />
      </div>
    </div>
    <div>
      <button
        :aria-label="
          fieldOfLaw.identifier + ' ' + fieldOfLaw.text + ' entfernen'
        "
        class="material-icons text-blue-800"
        @click="emit('remove-from-list')"
      >
        delete_outline
      </button>
    </div>
  </div>
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
