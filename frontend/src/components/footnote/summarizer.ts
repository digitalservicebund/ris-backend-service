import { createTextVNode, VNode } from "vue"
import { Footnote, FOOTNOTE_LABELS } from "@/components/footnote/types"
import { MetadatumType } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export function summarizeFootnotePerLine(data?: Footnote): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const prefix =
    data?.FOOTNOTE?.filter((footnote) =>
      Object.keys(footnote).includes(MetadatumType.FOOTNOTE_REFERENCE),
    )[0]?.FOOTNOTE_REFERENCE[0] ?? undefined

  if (prefix) {
    summarizerData.push(
      new SummarizerDataSet([prefix], { type: Type.BOLD_IN_ONE_LINE }),
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
        summarizerData.push(
          new SummarizerDataSet([FOOTNOTE_LABELS[partType], content.trim()], {
            type: Type.FOOTNOTE,
          }),
        )
      }
    })
  }

  return normsMetadataSummarizer(summarizerData, "")
}
