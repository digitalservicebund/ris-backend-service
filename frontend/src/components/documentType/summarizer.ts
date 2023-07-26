import { createTextVNode, VNode } from "vue"
import { NORM_CATEGORY_TRANSLATIONS } from "@/components/divergentGroup/divergentSummaryFunctions"
import { Metadata } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export function documentTypeSummarizer(data?: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const typeName = data?.TYPE_NAME?.[0]
  if (typeName) {
    summarizerData.push(new SummarizerDataSet([typeName]))
  }

  const categories =
    data?.NORM_CATEGORY?.filter((category) => category != null) ?? []
  if (categories.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(
        categories.map((category) => NORM_CATEGORY_TRANSLATIONS[category]),
        { type: Type.CHECKMARK },
      ),
    )
  }

  const templateNames = data?.TEMPLATE_NAME ?? []

  if (templateNames.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(templateNames, { separator: "," }),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}
