<script lang="ts" setup>
import { updateDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"
import { DocUnit } from "../../types/DocUnit"
import EditorVmodel from "../EditorVmodel.vue"
import RisButton from "../ris-button/RisButton.vue"

const store = useDocUnitsStore()

interface KurzUndLangtexteListEntry {
  id: keyof DocUnit
  name: string
  label: string
  aria: string
}

const kurzUndLangtexteDef: KurzUndLangtexteListEntry[] = []

const add = (id: keyof DocUnit, name: string) => {
  kurzUndLangtexteDef.push({
    id: id,
    name: name,
    label: name,
    aria: name,
  })
}

add("entscheidungsname", "Entscheidungsname")
add("titelzeile", "Titelzeile")
add("leitsatz", "Leitsatz")
add("orientierungssatz", "Orientierungssatz")
add("tenor", "Tenor")
add("gruende", "Gründe")
add("tatbestand", "Tatbestand")
add("entscheidungsgruende", "Entscheidungsgründe")

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
        <v-col><h1>Kurz- & Langtexte</h1></v-col>
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
                />
              </div>
            </span>
          </div>
          <div class="ris-texte-form__textfield">
            <RisButton @click="onSaveClick" />
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
