import dayjs from "dayjs"
import { createTextVNode, VNode } from "vue"
import { Metadata, MetadataSections } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

export function officialReferenceSummarizer(data: MetadataSections): VNode {
  if (!data) return createTextVNode("")

  if (data.PRINT_ANNOUNCEMENT) {
    return printAnnouncementSummary(data.PRINT_ANNOUNCEMENT[0])
  } else if (data.DIGITAL_ANNOUNCEMENT) {
    return digitalAnnouncementSummary(data.DIGITAL_ANNOUNCEMENT[0])
  } else if (data.EU_ANNOUNCEMENT) {
    return euAnnouncementSummary(data.EU_ANNOUNCEMENT[0])
  } else if (data.OTHER_OFFICIAL_ANNOUNCEMENT) {
    return otherOfficialReferenceSummary(data.OTHER_OFFICIAL_ANNOUNCEMENT[0])
  }
  return createTextVNode("")
}
export function printAnnouncementSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const midSection = [
    data.ANNOUNCEMENT_GAZETTE?.[0],
    data.YEAR?.[0],
    data.NUMBER?.[0],
    data.PAGE?.[0],
  ].filter((f): f is string => f !== undefined)

  const additionalInfos = data.ADDITIONAL_INFO ?? []
  const explanations = data.EXPLANATION ?? []

  if (
    midSection.length > 0 ||
    additionalInfos.length > 0 ||
    explanations.length > 0
  ) {
    summarizerData.push(new SummarizerDataSet(["Papierverkündungsblatt"]))
  }
  if (midSection.length > 0) {
    summarizerData.push(new SummarizerDataSet(midSection, { separator: "," }))
  }

  if (additionalInfos.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(additionalInfos, { type: Type.CHIP }),
    )
  }
  if (explanations.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(explanations, { type: Type.CHIP }),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}

export function digitalAnnouncementSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const date = data.DATE?.[0]
    ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
    : undefined

  const midSection = [
    data.ANNOUNCEMENT_MEDIUM?.[0],
    date,
    data.EDITION?.[0],
    data.YEAR?.[0],
    data.PAGE?.[0],
    data.AREA_OF_PUBLICATION?.[0],
    data.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0],
  ].filter((f): f is string => f !== undefined)

  const additionalInfos = data.ADDITIONAL_INFO ?? []
  const explanations = data.EXPLANATION ?? []

  if (
    midSection.length > 0 ||
    additionalInfos.length > 0 ||
    explanations.length > 0
  ) {
    summarizerData.push(
      new SummarizerDataSet(["Elektronisches Verkündungsblatt"]),
    )
  }

  if (midSection.length > 0) {
    summarizerData.push(new SummarizerDataSet(midSection, { separator: "," }))
  }

  if (additionalInfos.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(additionalInfos, { type: Type.CHIP }),
    )
  }
  if (explanations.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(explanations, { type: Type.CHIP }),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}

export function euAnnouncementSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const midSection = [
    data.EU_GOVERNMENT_GAZETTE?.[0],
    data.YEAR?.[0],
    data.SERIES?.[0],
    data.NUMBER?.[0],
    data.PAGE?.[0],
  ].filter((f): f is string => f !== undefined)
  const additionalInfos = data.ADDITIONAL_INFO ?? []
  const explanations = data.EXPLANATION ?? []

  if (
    midSection.length > 0 ||
    additionalInfos.length > 0 ||
    explanations.length > 0
  ) {
    summarizerData.push(new SummarizerDataSet(["Amtsblatt der EU"]))
  }
  if (midSection.length > 0) {
    summarizerData.push(new SummarizerDataSet(midSection, { separator: "," }))
  }
  if (additionalInfos.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(additionalInfos, { type: Type.CHIP }),
    )
  }
  if (explanations.length > 0) {
    summarizerData.push(
      new SummarizerDataSet(explanations, { type: Type.CHIP }),
    )
  }

  return normsMetadataSummarizer(summarizerData)
}

export function otherOfficialReferenceSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []
  const otherOfficialReference = data.OTHER_OFFICIAL_REFERENCE?.[0]

  if (otherOfficialReference) {
    summarizerData.push(new SummarizerDataSet(["Sonstige amtliche Fundstelle"]))
    summarizerData.push(new SummarizerDataSet([otherOfficialReference]))
  }

  return normsMetadataSummarizer(summarizerData)
}
