<script lang="ts" setup>
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

interface Props {
  fieldOfLaw: FieldOfLawNode
  showBin?: boolean
}

const props = defineProps<Props>()

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
  <div class="flex mt-20">
    <div class="flex flex-col grow">
      <div class="flex">
        <div class="flex label-02-reg pt-8 text-blue-800">
          <span
            :aria-label="
              props.fieldOfLaw.identifier +
              ' ' +
              props.fieldOfLaw.text +
              ' im Sachgebietsbaum anzeigen'
            "
            class="identifier link"
            @click="emit('node-clicked')"
            @keyup.enter="emit('node-clicked')"
          >
            {{ props.fieldOfLaw.identifier }}
          </span>
          <span class="text-wrapper">
            <TokenizeText
              :keywords="props.fieldOfLaw.linkedFields ?? []"
              :text="props.fieldOfLaw.text"
              @link-token:clicked="handleTokenClick"
            />
          </span>
        </div>
      </div>
    </div>
    <div v-if="props.showBin">
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
  <hr class="border-blue-500 mt-8 w-full" />
</template>

<style lang="scss" scoped>
.identifier {
  width: 50px;
  margin-right: 10px;
  white-space: nowrap;
}

.text-wrapper {
  margin-left: 105px;
  color: black;
}

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
