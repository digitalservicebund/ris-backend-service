<script lang="ts" setup>
import { updateDocUnit } from "../api/docUnitService"
import { useDocUnitsStore } from "../store"
import { DocUnit } from "../types/DocUnit"
import { FieldSize } from "../types/FieldSize"
import EditorVmodel from "./EditorVmodel.vue"
import SimpleButton from "./SimpleButton.vue"

const store = useDocUnitsStore()

interface KurzUndLangtexteListEntry {
  id: keyof DocUnit
  name: string
  label: string
  aria: string
  fieldSize: FieldSize
}

const kurzUndLangtexteDef: KurzUndLangtexteListEntry[] = []

const add = (id: keyof DocUnit, name: string, fieldSize: FieldSize) => {
  kurzUndLangtexteDef.push({
    id: id,
    name: name,
    label: name,
    aria: name,
    fieldSize: fieldSize,
  })
}

add("entscheidungsname", "Entscheidungsname", "small")
add("titelzeile", "Titelzeile", "small")
add("leitsatz", "Leitsatz", "medium")
add("orientierungssatz", "Orientierungssatz", "small")
add("tenor", "Tenor", "medium")
add("gruende", "Gründe", "large")
add("tatbestand", "Tatbestand", "large")
add("entscheidungsgruende", "Entscheidungsgründe", "large")

const onSaveClick = () => {
  updateDocUnit(store.getSelected()).then((updatedDocUnit) => {
    store.update(updatedDocUnit)
  })
  alert("Kurz- & Langtexte wurden gespeichert")
}
</script>

<template>
  <div v-if="!store.hasSelected()">Loading...</div>
  <div v-else>
    <form novalidate class="ris-texte-form">
      <v-row>
        <v-col><h2>Kurz- & Langtexte</h2></v-col>
      </v-row>
      <v-row>
        <v-col>
          <div
            v-for="item in kurzUndLangtexteDef"
            :key="item.id"
            class="ris-texte-form__textfield"
          >
            <span class="ris-texte-form__label">
              {{ item.label }}
              <div>
                <EditorVmodel
                  v-model="store.getSelectedSafe()[item.id]"
                  class="ris-texte-form__input"
                  :field-size="item.fieldSize"
                />
              </div>
            </span>
          </div>
          <div class="ris-texte-form__textfield">
            <SimpleButton @click="onSaveClick" />
          </div>
        </v-col>
      </v-row>
    </form>
  </div>
</template>

<style lang="scss">
.ris-texte-form {
  padding: rem(20px);

  &__textfield {
    padding: rem(20px);
  }

  &__input {
    width: 100%;
    // padding: 17px 24px;
    margin-top: 5px;
    outline: 2px solid $text-tertiary;
    resize: vertical;

    &:hover,
    &:focus {
      // outline-width: 4px;
    }
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
</style>
