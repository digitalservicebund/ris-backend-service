<script lang="ts" setup>
import { ref } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FootnoteInput from "@/components/footnotes/FootnoteInput.vue"
import { summarizeFootnotePerLine } from "@/components/footnotes/summarizer"
import { Footnote } from "@/components/footnotes/types"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"

const EXAMPLE_FOOTNOTES = [
  {
    FOOTNOTE: [
      { FOOTNOTE_REFERENCE: ["§ 7 Abs. 1a Satz 1 u. 2"] },
      {
        FOOTNOTE_CHANGE: [
          "eine ganze Menge Text mit viel Inhalt über eine Zeile hinaus und noch viel viel viel weiter in die nächste",
        ],
      },
      { FOOTNOTE_EU_LAW: ["irgendwas halt"] },
    ],
  },
  {
    FOOTNOTE: [
      { FOOTNOTE_STATE_LAW: [""] },
      { FOOTNOTE_COMMENT: ["einfach nur ein Kommentar"] },
      { FOOTNOTE_DECISION: ["das wurde halt so entschieden"] },
    ],
  },
  {
    FOOTNOTE: [
      { FOOTNOTE_REFERENCE: ["§ 1 Abs. 5b"] },
      {
        FOOTNOTE_OTHER: [
          "noch etwas mehr oben drauf\nmit einer neuen Zeile\nund noch einer\n",
          "ach nochmal eben etwas",
        ],
      },
      {
        FOOTNOTE_CHANGE: [
          "eine ganze Menge Text mit viel Inhalt über eine Zeile hinaus und noch viel viel viel weiter in die nächste",
        ],
      },
      { FOOTNOTE_OTHER: ["ach nochmal eben etwas"] },
    ],
  },
] as Footnote[]

const inputValueForExamples = ref<Footnote[]>(EXAMPLE_FOOTNOTES)

const footnoteLineSummary = withSummarizer(summarizeFootnotePerLine)
</script>

<template>
  <div class="flex flex-col p-64 w-[60rem]">
    <ExpandableDataSet
      :data-set="inputValueForExamples"
      :summary-component="footnoteLineSummary"
      title="Fußnoten"
    >
      <EditableList
        v-model="inputValueForExamples"
        :default-value="{}"
        :edit-component="FootnoteInput"
        :summary-component="footnoteLineSummary"
      />
    </ExpandableDataSet>
  </div>
</template>
