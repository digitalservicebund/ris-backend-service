<script lang="ts" setup>
import { h, ref, VNode } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FootnoteInput from "@/components/Footnotes/FootnoteInput.vue"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"
import {
  Footnote,
  FOOTNOTE_TYPE_TO_LABEL_MAPPING,
  FootnoteSection,
  FootnoteSectionType
} from "@/components/Footnotes/types";


function summarizeFootnotePart(
  part: FootnoteSection,
  extraTypeClasses = [""]
): VNode {
  const typeClasses = [
    "bg-yellow-300",
    "rounded",
    "px-6",
    "py-2",
    "whitespace-pre",
    ...extraTypeClasses,
  ]
  const typeLabel = part.type ?? "Unbekannt"
  const type = h("span", { class: typeClasses }, typeLabel)
  const contentClasses = ["pl-6", "pr-10", "inline", "whitespace-pre-wrap"]
  const contentText = h("p", { class: contentClasses }, part.content?.trim())
  const contentEmptyHint = h(
    "span",
    { class: [...contentClasses, "text-gray-600"] },
    "leer"
  )
  const hasContent = part.content && part.content.trim().length > 0
  const content = hasContent ? contentText : contentEmptyHint
  return h("div", [type, content])
}
function summarizePrefix(prefix?: string): VNode | string {
  const prefixNode = h(
    "span",
    { class: "pr-10" },
    prefix?.trim().replaceAll(/\n/g, "<br>")
  )
  const hasPrefix = prefix && prefix.trim().length > 0
  return hasPrefix ? prefixNode : ""
}
function summarizeFootnotePerLine(footnote: Footnote): VNode {
  const partNodes = footnote.parts.map(({ type, content }) =>
    h(
      "div",
      summarizeFootnotePart({
        type: type && FOOTNOTE_TYPE_TO_LABEL_MAPPING[type],
        content,
      })
    )
  )
  const prefix = summarizePrefix(footnote.prefix)
  return h("div", { class: ["flex", "flex-col", "gap-10"] }, [
    prefix,
    partNodes,
  ])
}
const EXAMPLE_FOOTNOTES: Footnote[] = [
  {
    prefix: "§ 7 Abs. 1a Satz 1 u. 2",
    parts: [
      {
        type: FootnoteSectionType.FOOTNOTE_CHANGE,
        content:
          "eine ganze Menge Text mit viel Inhalt über eine Zeile hinaus und noch viel viel viel weiter in die nächste",
      },
      {
        type: FootnoteSectionType.FOOTNOTE_EU_LAW,
        content: "irgendwas halt",
      },
    ],
  },
  {
    parts: [
      { type: FootnoteSectionType.FOOTNOTE_STATE_LAW, content: "" },
      {
        type: FootnoteSectionType.FOOTNOTE_COMMENT,
        content: "einfach nur ein Kommentar",
      },
      {
        type: FootnoteSectionType.FOOTNOTE_DECISION,
        content: "das wurde halt so entschieden",
      },
    ],
  },
  {
    prefix: "§ 1 Abs. 5b",
    parts: [
      {
        type: FootnoteSectionType.FOOTNOTE_OTHER,
        content:
          "noch etwas mehr oben drauf\nmit einer neuen Zeile\nund noch einer\n",
      },
      {
        type: FootnoteSectionType.FOOTNOTE_OTHER,
        content: "ach nochmal eben etwas",
      },
    ],
  },
]
const inputValueForExamples = ref<Footnote[]>(
  JSON.parse(JSON.stringify(EXAMPLE_FOOTNOTES))
)

const FootnotePerLineSummary = withSummarizer(summarizeFootnotePerLine)
</script>

<template>
  <div class="flex flex-col p-64 w-[60rem]">
    <ExpandableDataSet
      :data-set="inputValueForExamples"
      :summary-component="FootnotePerLineSummary"
      title="Fußnoten"
    >
      <EditableList
        v-model="inputValueForExamples"
        :default-value="{ parts: [] }"
        :edit-component="FootnoteInput"
        :summary-component="FootnotePerLineSummary"
      />
    </ExpandableDataSet>
  </div>
</template>
