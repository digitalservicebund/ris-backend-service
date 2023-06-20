<script lang="ts" setup>
import { h, ref, VNode } from "vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FootnoteInput from "@/components/Footnotes/FootnoteInput.vue"
import {
  Footnote,
  FOOTNOTE_LABELS,
  FootnoteSection,
} from "@/components/Footnotes/types"
import { MetadataSections, MetadatumType } from "@/domain/Norm"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableList.vue"

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
  const typeLabel = part.type ? FOOTNOTE_LABELS[part.type] : "Unbekannt"
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
function summarizeFootnotePerLine(data: Footnote): VNode {
  const prefix =
    data.FOOTNOTE?.filter((footnote) =>
      Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE)
    )[0]?.FOOTNOTE_REFERENCE[0] ?? undefined
  const segments = data.FOOTNOTE?.filter(
    (footnote) =>
      !Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE)
  )?.map((footnote) =>
    h(
      "div",
      summarizeFootnotePart({
        type: MetadatumType[
          Object.keys(footnote)[0] as keyof typeof MetadatumType
        ],
        content: Object.values(footnote)[0][0],
      })
    )
  )
  return h("div", { class: ["flex", "flex-col", "gap-10"] }, [
    prefix ? summarizePrefix(prefix) : "",
    segments,
  ])
}

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
]

const inputValueForExamples = ref<MetadataSections[]>(EXAMPLE_FOOTNOTES)

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
        :default-value="{}"
        :edit-component="FootnoteInput"
        :summary-component="FootnotePerLineSummary"
      />
    </ExpandableDataSet>
  </div>
</template>
