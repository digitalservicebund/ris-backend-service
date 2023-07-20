import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
} from "@/helpers/normsMetadataSummarizer"

export function leadSummarizer(data: Metadata): VNode {
  if (!data) return createTextVNode("")
  const summarizerData: SummarizerDataSet[] = []

  const jurisdiction = data.LEAD_JURISDICTION?.[0]
  if (jurisdiction) {
    summarizerData.push(new SummarizerDataSet([jurisdiction]))
  }
  const unit = data.LEAD_UNIT?.[0]
  if (unit) {
    summarizerData.push(new SummarizerDataSet([unit]))
  }
  return normsMetadataSummarizer(summarizerData)
}
