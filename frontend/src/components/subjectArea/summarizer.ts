import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
} from "@/helpers/normsMetadataSummarizer"

export function subjectAreaSummarizer(data: Metadata): VNode {
  if (!data) return createTextVNode("")
  const summarizerData: SummarizerDataSet[] = []

  const fna = data.SUBJECT_FNA?.[0]
  if (fna) {
    summarizerData.push(new SummarizerDataSet([`FNA-Nummer ${fna}`]))
  }
  const previousFna = data.SUBJECT_PREVIOUS_FNA?.[0]
  if (previousFna) {
    summarizerData.push(
      new SummarizerDataSet([`Frühere FNA-Nummer ${previousFna}`]),
    )
  }
  const gesta = data.SUBJECT_GESTA?.[0]
  if (gesta) {
    summarizerData.push(new SummarizerDataSet([`GESTA-Nummer ${gesta}`]))
  }
  const bgb3 = data.SUBJECT_BGB_3?.[0]
  if (bgb3) {
    summarizerData.push(
      new SummarizerDataSet([`Bundesgesetzblatt Teil III ${bgb3}`]),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}
