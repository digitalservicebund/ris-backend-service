<script lang="ts" setup>
import { ref } from "vue"
import TextEditor from "@/components/input/TextEditor.vue"

const text = `<div>LanguageTool ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. <noindex>Rechtshcreibfehler</noindex> werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, zB. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten. </div>`

const htmlContent = ref(text)

const updateContent = (newValue: string) => {
  htmlContent.value = newValue
}
</script>
<template>
  <TextEditor
    aria-label="Dokumentenvorschau"
    data-testid="text-editor"
    editable
    element-id="text-editor"
    field-size="max"
    plain-border-numbers
    :value="htmlContent"
    @update-value="updateContent"
  />

  <div>
    {{ htmlContent }}
  </div>
</template>
