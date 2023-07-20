import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export function summarizeAnnouncementDate(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  if (data.YEAR?.length) {
    summarizerData.push(new SummarizerDataSet([data.YEAR.toString()]))
  } else if (data.DATE?.length) {
    summarizerData.push(
      new SummarizerDataSet([data.DATE[0]], {
        type: Type.DATE,
        format: "DD.MM.YYYY",
      }),
    )
    if (data.TIME?.length) {
      summarizerData.push(new SummarizerDataSet([data.TIME[0]]))
    }
  }

  return normsMetadataSummarizer(summarizerData, "")
}
