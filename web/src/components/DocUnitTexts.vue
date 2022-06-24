<script lang="ts" setup>
import { computed } from "vue"
import { Texts } from "../domain/DocUnit"
import * as iconsAndLabels from "../iconsAndLabels.json"
import SimpleButton from "./SimpleButton.vue"
import TextEditor from "./TextEditor.vue"
import { FieldSize } from "@/domain/FieldSize"

const props = defineProps<{ texts: Texts }>()
const emit = defineEmits<{
  (e: "updateValue", updatedValue: [keyof Texts, string]): void
  (e: "updateDocUnit"): void
}>()

const data = computed(() =>
  iconsAndLabels.texts.map((item) => {
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
  <div v-if="!data">Loading...</div>
  <div v-else>
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
                  :field-size="item.fieldSize"
                  @update-value="emit('updateValue', [item.id, $event])"
                />
              </div>
            </span>
          </div>
          <div class="ris-texte-form__textfield">
            <SimpleButton @click="emit('updateDocUnit')" />
          </div>
        </v-col>
      </v-row>
    </form>
  </div>
</template>

<style lang="scss">
.editor-btn {
  border: 1px solid black;
  background: white;
  color: black;
  border-radius: 0.3rem;
  margin: 0.1rem;
  padding: 0.1rem 0.4rem;
  &__active {
    background: black;
    color: white;
  }
}
.ris-texte-form {
  padding: rem(20px);

  &__textfield {
    padding: rem(20px);
  }

  &__input {
    width: 100%;
    // padding: 17px 24px;
    margin-top: 5px;
    outline: 2px solid $blue800;
    resize: vertical;

    &:hover,
    &:focus {
      outline-width: 4px;
    }
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
</style>
