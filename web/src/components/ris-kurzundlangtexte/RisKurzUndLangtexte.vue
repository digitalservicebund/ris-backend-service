<script lang="ts" setup>
import { EditorContent, Editor } from "@tiptap/vue-3"
import { useDocUnitsStore } from "../../store"
import { DocUnit } from "../../types/DocUnit"
import RisButton from "../ris-button/RisButton.vue"

const store = useDocUnitsStore()

interface KurzUndLangtexteListEntry {
  id: keyof DocUnit
  name: string
  label: string
  aria: string
  editor: Editor
}

const kurzUndLangtexteDef: KurzUndLangtexteListEntry[] = []

const add = (id: keyof DocUnit, name: string, editor: Editor) => {
  kurzUndLangtexteDef.push({
    id: id,
    name: name,
    label: name,
    aria: name,
    editor: editor,
  })
}

const buildEditor = () => {
  return new Editor({
    extensions: [],
  })
}

add("entscheidungsname", "Entscheidungsname", buildEditor())
add("titelzeile", "Titelzeile", buildEditor())
add("leitsatz", "Leitsatz", buildEditor())
add("orientierungssatz", "Orientierungssatz", buildEditor())
add("tenor", "Tenor", buildEditor())
add("gruende", "Gründe", buildEditor())
add("tatbestand", "Tatbestand", buildEditor())
add("entscheidungsgruende", "Entscheidungsgründe", buildEditor())

const onSaveClick = () => {
  // updateDocUnit(store.getSelected()).then((updatedDocUnit) => {
  //   store.update(updatedDocUnit)
  // })
  alert("Daten wurden (noch) nicht gespeichert, work in progress :)")
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
                <editor-content
                  :editor="item.editor"
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
.ProseMirror {
  height: 100px;
  background: #eee;
  color: #000;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
}
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
