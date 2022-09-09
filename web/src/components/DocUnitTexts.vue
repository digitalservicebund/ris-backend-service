<script lang="ts" setup>
import { computed } from "vue"
import { Texts } from "../domain/docUnit"
import SaveDocUnitButton from "./SaveDocUnitButton.vue"
import TextEditor from "./TextEditor.vue"
import { texts } from "@/domain"
import { FieldSize } from "@/domain/FieldSize"

const props = defineProps<{ texts: Texts; updateStatus: number }>()
const emit = defineEmits<{
  (e: "updateValue", updatedValue: [keyof Texts, string]): Promise<void>
  (e: "updateDocUnit"): Promise<void>
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
  <form novalidate class="ris-texte-form">
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
                :value="item.value"
                class="ris-texte-form__input"
                :aria-label="item.aria"
                :field-size="item.fieldSize"
                @update-value="emit('updateValue', [item.id, $event])"
              />
            </div>
          </span>
        </div>
        <div class="ris-texte-form__textfield">
          <SaveDocUnitButton
            aria-label="Kurz- und Langtexte Speichern Button"
            :update-status="updateStatus"
            @update-doc-unit="emit('updateDocUnit')"
          />
        </div>
      </v-col>
    </v-row>
  </form>
</template>

<style lang="scss">
.ris-texte-form {
  padding: rem(20px);
  &__textfield {
    padding: rem(20px);
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
