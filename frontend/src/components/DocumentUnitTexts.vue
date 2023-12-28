<script lang="ts" setup>
import { computed } from "vue"
import { Texts } from "../domain/documentUnit"
import TextEditor from "../shared/components/input/TextEditor.vue"
import { texts as textsFields } from "@/fields/caselaw"

const props = defineProps<{ texts: Texts }>()

const emit = defineEmits<{
  updateValue: [updatedValue: [keyof Texts, string]]
}>()

const data = computed(() =>
  textsFields.map((item) => {
    return {
      id: item.name as keyof Texts,
      name: item.name,
      label: item.label,
      aria: item.label,
      value: props.texts[item.name as keyof Texts],
    }
  }),
)
</script>

<template>
  <div
    aria-label="Nachgehende Entscheidung"
    class="core-data mb-16 flex flex-col gap-24 bg-white p-32"
  >
    <h2 class="ds-heading-03-bold">Kurz- & Langtexte</h2>

    <div class="flex flex-col gap-24">
      <div v-for="item in data" :key="item.id" class="">
        <label class="ds-label-02-reg mb-4" :for="item.id">{{
          item.label
        }}</label>

        <TextEditor
          :id="item.id"
          :aria-label="item.aria"
          class="outline outline-2 outline-blue-900"
          editable
          :value="item.value"
          @update-value="emit('updateValue', [item.id, $event])"
        />
      </div>
    </div>
  </div>
</template>
