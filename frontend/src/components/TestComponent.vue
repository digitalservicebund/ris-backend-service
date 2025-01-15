<script lang="ts" setup>
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { BubbleMenu, Editor, EditorContent, useEditor } from "@tiptap/vue-3"
import { computed, ref } from "vue"

import TextCorrectionDropdown from "@/components/TextCorrectionDropdown.vue"
import { LanguageTool } from "@/editor/languagetool/languageTool"
import {
  LanguageToolHelpingWords,
  Match,
  Replacement,
} from "@/types/languagetool"

const editor = useEditor({
  content: `1
  LanguageTool ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtshcreibfehler werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, zB. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten.
In der Sache ist streitig, ob der Klägerin und Revisionsklägerin (Klägerin), ei­ner GmbH, die (unter anderem) Wohnungen an Senioren vermietete, im Streitjahr 2012 die erweiterte Gewerbesteuerkürzung gemäß § 9 Nr. 1 Satz 2 des Gewerbesteuergesetzes (GewStG) zusteht.



2
A und B waren zu jeweils 50 % Gesellschafter und Ge­schäftsführer der Klägerin. Sie waren Eigentümer des streitgegenständlichen Grundstücks. Auf dem Nachbargrundstück betrieb eine GmbH & Co. KG (KG) ein Hotel mit Restaurant. Kommanditisten der KG waren im Streitjahr A und B zu jeweils 50 %. Komplementärin der KG war eine GmbH, aber nicht die Klä­gerin. Das streitgegenständliche Grundstück überließen A und B der Klägerin, die dort vor dem Streitjahr eine Seniorenresidenz errichtete, zu der 33 Woh­nungen, eine Praxis und sonstige Geschäftsräume, darunter ein Café und ein Speisesaal, sowie Außenanlagen gehörten. A und B verpflichteten sich im Ge­genzug privatschriftlich "auf Dauer, mindestens bis zum Ablauf der Nutzungs­dauer der errichteten Bauten etc., von jeglichen Einwirkungen auf das Grund­stück Abstand zu nehmen" und "die tatsächliche Sachherrschaft über das Grundstück" auf die Klägerin zu übertragen; A und B seien sich darüber klar, dass hierdurch "Herausgabeansprüche [ihres] Eigentums" nicht mehr bestün­den. Des Weiteren verpflichteten sich A und B, auf Verlangen der Klägerin ihr auch zivilrechtlich Eigentum an dem Grundstück zu übertragen.



3
Vor dem Streitjahr "verkaufte" die Klägerin der KG mit privatschriftlichem "Kaufvertrag" unter anderem das Café und den Speisesaal, die als "Bauten (Café 246 qm)" bezeichnet wurden, für rund 208.500 €.`,
  extensions: [
    Document,
    Paragraph,
    Text,
    LanguageTool.configure({
      automaticMode: true,
      documentId: "1",
      apiUrl: "http://localhost:8081/v2/check", // replace this with your actual url
    }),
  ],
  onUpdate({ editor }) {
    setTimeout(() => updateMatch(editor as Editor))
  },
  onSelectionUpdate({ editor }) {
    setTimeout(() => updateMatch(editor as Editor))
  },
  onTransaction({ transaction: tr }) {
    if (tr.getMeta(LanguageToolHelpingWords.LoadingTransactionName))
      loading.value = true
    else loading.value = false
  },
})

const shouldShow = () => {
  if (editor.value == undefined) return false

  const match = editor.value.storage.languagetool.match
  const matchRange = editor.value.storage.languagetool.matchRange

  const { from, to } = editor.value.state.selection

  return (
    !!match && !!matchRange && matchRange.from <= from && to <= matchRange.to
  )
}

const match = ref<Match>()

const matchRange = ref<{ from: number; to: number }>()

const loading = ref(false)

const updateMatch = (editor: Editor) => {
  match.value = editor.storage.languagetool.match
  matchRange.value = editor.storage.languagetool.matchRange
}

const replacements = computed(() => match.value?.replacements || [])

// const matchMessage = computed(() => match.value?.message || "No Message")

const acceptSuggestion = (sug: Replacement) => {
  if (editor.value == undefined || matchRange.value == undefined) return
  editor.value.commands.insertContentAt(matchRange.value, sug.value)
}

const ignoreSuggestion = () => {
  if (editor.value == undefined) return

  editor.value.commands.ignoreLanguageToolSuggestion()
}
</script>

<template>
  <div>
    <EditorContent v-if="editor" class="content" :editor="editor" />

    <BubbleMenu
      v-if="editor"
      class="bubble-menu"
      :editor="editor"
      :should-show="shouldShow"
      :tippy-options="{ placement: 'bottom', animation: 'fade' }"
    >
      <TextCorrectionDropdown
        match-message=""
        :replacements="replacements"
        @suggestion:ignore="ignoreSuggestion"
        @suggestion:update="acceptSuggestion"
      />
    </BubbleMenu>
  </div>
</template>
<style lang="scss" scoped>
.ProseMirror {
  .lt {
    border-bottom: 2px solid #e86a69;
    transition: 0.25s ease-in-out;

    &:hover {
      background: rgba($color: #e86a69, $alpha: 20%);
    }

    &-style {
      border-bottom: 2px solid #9d8eff;

      &:hover {
        background: rgba($color: #9d8eff, $alpha: 20%) !important;
      }
    }

    &-typographical,
    &-grammar {
      border-bottom: 2px solid #eeb55c;

      &:hover {
        background: rgba($color: #eeb55c, $alpha: 20%) !important;
      }
    }

    &-misspelling {
      border-bottom: 2px solid #e86a69;

      &:hover {
        background: rgba($color: #e86a69, $alpha: 20%) !important;
      }
    }
  }

  &-focused {
    outline: none !important;
  }
}

.bubble-menu > .bubble-menu-section-container {
  display: flex;
  max-width: 400px;
  flex-direction: column;
  padding: 8px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 0 10px rgba($color: black, $alpha: 25%);

  .suggestions-section {
    display: flex;
    flex-flow: row wrap;
    margin-top: 1em;
    gap: 4px;

    .suggestion {
      display: flex;
      max-width: fit-content;
      align-items: center;
      padding: 4px;
      border-radius: 4px;
      background-color: #229afe;
      color: white;
      cursor: pointer;
      font-size: 1.1em;
      font-weight: 500;
    }
  }
}
</style>
