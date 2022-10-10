<script lang="ts" setup>
import { computed } from "vue"
import { Texts } from "../domain/documentUnit"
import SaveDocumentUnitButton from "./SaveDocumentUnitButton.vue"
import TextEditor from "./TextEditor.vue"
import { texts } from "@/domain"
import { FieldSize } from "@/domain/FieldSize"

const props = defineProps<{ texts: Texts; updateStatus: number }>()
const emit = defineEmits<{
  (e: "updateValue", updatedValue: [keyof Texts, string]): Promise<void>
  (e: "updateDocumentUnit"): Promise<void>
}>()
const data = computed(() =>
  texts.map((item) => {
    return {
      id: item.name as keyof Texts,
      name: item.name,
      label: item.label,
      aria: item.label,
      fieldSize: item.fieldSize as FieldSize,
      value: props.texts[item.name as keyof Texts],
    }
  })
)
</script>

<template>
  <div class="mb-[4rem]">
    <h1 class="heading-02-regular mb-[1rem]">Kurz- & Langtexte</h1>

    <div class="flex flex-col gap-36">
      <div v-for="item in data" :key="item.id" class="">
        <label class="label-02-regular mb-2" :for="item.id">{{
          item.label
        }}</label>

        <TextEditor
          :id="item.id"
          :aria-label="item.aria"
          class="outline outline-2 outline-blue-900"
          editable
          :field-size="item.fieldSize"
          :value="item.value"
          @update-value="emit('updateValue', [item.id, $event])"
        />
      </div>

      <SaveDocumentUnitButton
        aria-label="Kurz- und Langtexte Speichern Button"
        :update-status="updateStatus"
        @update-document-unit="emit('updateDocumentUnit')"
      />
    </div>
  </div>
</template>
