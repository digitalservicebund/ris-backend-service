import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
} from "@/helpers/normsMetadataSummarizer"

export function ageIndicationSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const rangeStart = data?.RANGE_START?.[0]
  if (rangeStart) {
    summarizerData.push(new SummarizerDataSet([rangeStart]))
  }

  const rangeEnd = data?.RANGE_END?.[0]
  if (rangeEnd) {
    summarizerData.push(new SummarizerDataSet([rangeEnd]))
  }

  return normsMetadataSummarizer(summarizerData)
}
