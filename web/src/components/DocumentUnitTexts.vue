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
  <form class="ris-texte-form" novalidate>
    <v-row>
      <v-col><h2>Kurz- & Langtexte</h2></v-col>
    </v-row>
    <v-row>
      <v-col>
        <div
          v-for="item in data"
          :key="item.id"
          class="ris-texte-form__textfield"
        >
          <span class="ris-texte-form__label">
            {{ item.label }}
            <div>
              <TextEditor
                :aria-label="item.aria"
                class="ris-texte-form__input"
                editable
                :field-size="item.fieldSize"
                :value="item.value"
                @update-value="emit('updateValue', [item.id, $event])"
              />
            </div>
          </span>
        </div>
        <div class="ris-texte-form__textfield">
          <SaveDocumentUnitButton
            aria-label="Kurz- und Langtexte Speichern Button"
            :update-status="updateStatus"
            @update-document-unit="emit('updateDocumentUnit')"
          />
        </div>
      </v-col>
    </v-row>
  </form>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.ris-texte-form {
  padding: 3rem 1rem;

  &__textfield {
    padding: 1rem;
  }

  &__input {
    width: 100%;
    margin-top: 5px;
    outline: 2px solid $text-tertiary;
    resize: vertical;
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
</style>
