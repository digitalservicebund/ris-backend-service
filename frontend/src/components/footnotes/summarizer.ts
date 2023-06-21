import { h, VNode } from "vue"
import {
  Footnote,
  FOOTNOTE_LABELS,
  FootnoteSection,
} from "@/components/footnotes/types"
import { MetadatumType } from "@/domain/Norm"

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
  return h("span", { class: "leading-loose" }, [type, content])
}

function summarizePrefix(prefix?: string): VNode | string {
  const prefixNode = h(
    "span",
    { class: ["pr-10", "font-bold"] },
    prefix?.trim().replaceAll(/\n/g, "<br>")
  )
  const hasPrefix = prefix && prefix.trim().length > 0
  return hasPrefix ? prefixNode : ""
}

export function summarizeFootnotePerLine(data: Footnote): VNode {
  const prefix =
    data?.FOOTNOTE?.filter((footnote) =>
      Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE)
    )[0]?.FOOTNOTE_REFERENCE[0] ?? undefined
  const segments = data?.FOOTNOTE?.filter(
    (footnote) =>
      !Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE)
  )?.map((footnote) =>
    h(
      "span",
      summarizeFootnotePart({
        type: MetadatumType[
          Object.keys(footnote)[0] as keyof typeof MetadatumType
        ],
        content: Object.values(footnote)[0][0],
      })
    )
  )
  const content = h("div", segments)
  return h("div", { class: ["flex", "flex-col", "gap-10"] }, [
    prefix ? summarizePrefix(prefix) : "",
    content,
  ])
}
