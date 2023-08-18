import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
} from "@/helpers/normsMetadataSummarizer"

export function digitalEvidenceSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const link = data?.LINK?.[0]
  if (link) {
    summarizerData.push(new SummarizerDataSet([link]))
  }

  const relatedData = data?.RELATED_DATA?.[0]
  if (relatedData) {
    summarizerData.push(new SummarizerDataSet([relatedData]))
  }

  const externalDataNote = data?.EXTERNAL_DATA_NOTE?.[0]
  if (externalDataNote) {
    summarizerData.push(new SummarizerDataSet([externalDataNote]))
  }

  const appendix = data?.APPENDIX?.[0]
  if (appendix) {
    summarizerData.push(new SummarizerDataSet([appendix]))
  }

  return normsMetadataSummarizer(summarizerData)
}
