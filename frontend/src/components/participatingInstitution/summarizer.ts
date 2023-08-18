import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
} from "@/helpers/normsMetadataSummarizer"

export function participationSummarizer(data: Metadata): VNode {
  if (!data) return createTextVNode("")
  const summarizerData: SummarizerDataSet[] = []

  const type = data.PARTICIPATION_TYPE?.[0]
  if (type) {
    summarizerData.push(new SummarizerDataSet([type]))
  }
  const institution = data.PARTICIPATION_INSTITUTION?.[0]
  if (institution) {
    summarizerData.push(new SummarizerDataSet([institution]))
  }
  return normsMetadataSummarizer(summarizerData)
}
