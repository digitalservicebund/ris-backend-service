<script lang="ts" setup>
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

const props = defineProps<{ subject: FieldOfLawNode }>()
const emit = defineEmits<{
  (event: "remove-from-list"): void
  (event: "node-clicked"): void
}>()
</script>

<template>
  <div class="flex">
    <div class="flex flex-col grow">
      <div class="flex">
        <div>
          <button
            aria-label="Auswahl im Sachgebietsbaum"
            class="deeplink p-12 text-blue-800"
            @click="emit('node-clicked')"
          ></button>
        </div>
        <div class="label-02-reg pt-8 text-blue-800">
          <span
            class="link"
            @click="emit('node-clicked')"
            @keyup.enter="emit('node-clicked')"
          >
            {{ props.subject.identifier }}
          </span>
        </div>
      </div>
      <div class="grow label-03-reg pb-16 pt-4 text-blue-800">
        {{ props.subject.subjectFieldText }}
      </div>
    </div>
    <div>
      <button
        aria-label="Löschen"
        class="material-icons text-blue-800"
        @click="emit('remove-from-list')"
      >
        delete_outline
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
button.deeplink {
  background-image: url("@/assets/icons/deeplink_regular.svg");
  background-repeat: no-repeat;
  background-size: 18px;
}

.link {
  &:hover {
    cursor: pointer;
    text-decoration: underline;
  }

  &:active {
    cursor: pointer;
    text-decoration: underline;
    text-decoration-thickness: 4px;
  }

  &:focus {
    border: 4px solid #004b76;
    cursor: pointer;
    text-decoration: underline;
  }
}
</style>
