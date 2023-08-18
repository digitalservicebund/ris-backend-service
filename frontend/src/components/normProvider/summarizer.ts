import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export function normProviderSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const entity = data.ENTITY?.[0]
  if (entity) {
    summarizerData.push(new SummarizerDataSet([entity]))
  }
  const decidingBody = data.DECIDING_BODY?.[0]
  if (decidingBody) {
    summarizerData.push(new SummarizerDataSet([decidingBody]))
  }
  const isResolutionMajority = data.RESOLUTION_MAJORITY?.[0]

  if (isResolutionMajority) {
    summarizerData.push(
      new SummarizerDataSet(["Beschlussfassung mit qual. Mehrheit"], {
        type: Type.CHECKMARK,
      }),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}
