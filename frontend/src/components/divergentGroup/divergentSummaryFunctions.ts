import { createTextVNode, VNode } from "vue"
import { Metadata } from "@/domain/norm"
import { getLabel } from "@/helpers/generalSummarizer"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export const NORM_CATEGORY_TRANSLATIONS = {
  AMENDMENT_NORM: "Änderungsnorm",
  BASE_NORM: "Stammnorm",
  TRANSITIONAL_NORM: "Übergangsnorm",
}

export function divergentDefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const date = data.DATE?.[0]

  if (date) {
    summarizerData.push(
      new SummarizerDataSet([date], { type: Type.DATE, format: "DD.MM.YYYY" }),
    )
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

  return normsMetadataSummarizer(summarizerData)
}

export function divergentUndefinedSummary(data: Metadata): VNode {
  if (!data) return createTextVNode("")

  const summarizerData: SummarizerDataSet[] = []

  const undefinedDate = data?.UNDEFINED_DATE?.[0]

  if (undefinedDate) {
    summarizerData.push(new SummarizerDataSet([getLabel(undefinedDate)]))
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

  return normsMetadataSummarizer(summarizerData)
}
