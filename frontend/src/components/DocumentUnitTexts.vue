<script lang="ts" setup>
import { computed } from "vue"
import TextEditor from "../components/input/TextEditor.vue"
import { Texts } from "../domain/documentUnit"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import { BorderNumberLink } from "@/editor/borderNumberLink"
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
      fieldType: item.fieldType,
      fieldSize: item.fieldSize,
    }
  }),
)
</script>

<template>
  <div class="core-data mb-16 flex flex-col gap-24 bg-white p-32">
    <h2 class="ds-heading-03-bold">Kurz- & Langtexte</h2>

    <div class="flex flex-col gap-24">
      <div v-for="item in data" :key="item.id" class="">
        <label class="ds-label-02-reg mb-4" :for="item.id">{{
          item.label
        }}</label>

        <TextEditor
          v-if="item.fieldType == TextAreaInput"
          :id="item.id"
          :aria-label="item.aria"
          class="ml-2 pl-2 outline outline-2 outline-blue-900"
          editable
          :extensions="[
            BorderNumberLink.configure({
              validBorderNumbers: ['4', '5', '6'],
            }),
          ]"
          :field-size="item.fieldSize"
          :value="item.value"
          @update-value="emit('updateValue', [item.id, $event])"
        />

        <TextInput
          v-if="item.fieldType == TextInput"
          :id="item.id"
          :aria-label="item.aria"
          :model-value="item.value"
          size="medium"
          @update:model-value="emit('updateValue', [item.id, $event as string])"
        />
      </div>
    </div>
  </div>
</template>
