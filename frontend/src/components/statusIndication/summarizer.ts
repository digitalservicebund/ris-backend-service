import { createTextVNode, VNode } from "vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

function summarizeUpdate(
  type: MetadataSectionName.STATUS | MetadataSectionName.REISSUE,
  data: Metadata,
): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const typeName = type === MetadataSectionName.STATUS ? "Stand" : "Neufassung"
  if (typeName) {
    summarizerData.push(new SummarizerDataSet([typeName]))
  }
  const note = data?.NOTE?.[0]
  if (note) {
    summarizerData.push(new SummarizerDataSet([note]))
  }

  let descriptionOrArticle
  if (type === MetadataSectionName.STATUS) {
    descriptionOrArticle = data?.DESCRIPTION?.[0]
  } else if (type === MetadataSectionName.REISSUE) {
    descriptionOrArticle = data?.ARTICLE?.[0]
  }
  if (descriptionOrArticle) {
    summarizerData.push(new SummarizerDataSet([descriptionOrArticle]))
  }

  const date = data?.DATE?.[0]
  if (date) {
    summarizerData.push(
      new SummarizerDataSet([date], { type: Type.DATE, format: "DD.MM.YYYY" }),
    )
  }
  const year = data?.YEAR?.[0]
  if (year) {
    summarizerData.push(new SummarizerDataSet([year]))
  }

  const reference = data?.REFERENCE ?? []
  if (reference?.length > 0) {
    if (typeName === "Stand") {
      summarizerData.push(new SummarizerDataSet(reference, { type: Type.CHIP }))
    } else {
      summarizerData.push(new SummarizerDataSet(reference))
    }
  }
  return normsMetadataSummarizer(summarizerData)
}

function summarizeNote(
  type: MetadataSectionName.REPEAL | MetadataSectionName.OTHER_STATUS,
  data: string | undefined,
): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const typeName =
    type === MetadataSectionName.REPEAL ? "Aufhebung" : "Sonstiger Hinweis"
  if (typeName) {
    summarizerData.push(new SummarizerDataSet([typeName]))
  }
  if (data) {
    summarizerData.push(new SummarizerDataSet([data]))
  }

  return normsMetadataSummarizer(summarizerData)
}

export function summarizeStatusIndication(data: MetadataSections): VNode {
  if (!data) {
    return createTextVNode("")
  } else if (data.STATUS) {
    return summarizeUpdate(MetadataSectionName.STATUS, data.STATUS[0])
  } else if (data.REISSUE) {
    return summarizeUpdate(MetadataSectionName.REISSUE, data.REISSUE[0])
  } else if (data.REPEAL) {
    return summarizeNote(
      MetadataSectionName.REPEAL,
      data.REPEAL?.[0]?.TEXT?.[0],
    )
  } else if (data.OTHER_STATUS) {
    return summarizeNote(
      MetadataSectionName.OTHER_STATUS,
      data.OTHER_STATUS?.[0]?.NOTE?.[0],
    )
  }
  return createTextVNode("")
}
