import { createTextVNode, h, VNode } from "vue"
import { Footnote, FOOTNOTE_LABELS } from "@/components/footnote/types"
import { MetadatumType } from "@/domain/norm"

export function summarizeFootnotePerLine(data?: Footnote): VNode {
  if (!data) return createTextVNode("")

  const footnotesWithPrefix: [VNode | undefined, VNode[]] = [undefined, []]

  const prefix =
    data?.FOOTNOTE?.filter((footnote) =>
      Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE),
    )[0]?.FOOTNOTE_REFERENCE[0] ?? undefined

  if (prefix) {
    footnotesWithPrefix[0] = h(
      "span",
      { class: ["pr-10", "font-bold"] },
      prefix.trim().replace(/\n/g, "<br>"),
    )
  }

  const footnotes = data?.FOOTNOTE?.filter(
    (footnote) =>
      !Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE),
  )

  if (footnotes.length > 0) {
    footnotes?.forEach((footnote) => {
      const partType =
        MetadatumType[Object.keys(footnote)[0] as keyof typeof MetadatumType]
      const content = Object.values(footnote)[0][0]
      if (content && content.trim().length > 0) {
        const typeNode = h(
          "span",
          {
            class: [
              "bg-yellow-400",
              "rounded",
              "px-6",
              "py-2",
              "whitespace-pre",
            ],
          },
          FOOTNOTE_LABELS[partType],
        )
        const contentTextNode = h(
          "p",
          { class: ["pl-6", "pr-10", "inline", "whitespace-pre-wrap"] },
          content.trim(),
        )
        footnotesWithPrefix[1].push(
          h("span", { class: "leading-loose" }, [typeNode, contentTextNode]),
        )
      }
    })
  }

  if (footnotesWithPrefix[1].length > 0) {
    return h("div", { class: ["flex", "flex-col", "gap-10"] }, [
      footnotesWithPrefix[0],
      h("div", footnotesWithPrefix[1]),
    ])
  } else {
    return createTextVNode("")
  }
}
