import dayjs from "dayjs"
import { createTextVNode, VNode } from "vue"
import { Metadata, MetadataSections } from "@/domain/Norm"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
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

  const firstPart = [
    data.ANNOUNCEMENT_GAZETTE?.[0],
    data.YEAR?.[0],
    data.NUMBER?.[0],
  ].filter((f): f is string => f !== undefined)

  const page = data?.PAGE?.[0]

  if (firstPart.length > 0) {
    if (page) {
      firstPart[firstPart.length - 1] = `${firstPart[firstPart.length - 1]},`
    }
    summarizerData.push(new SummarizerDataSet(firstPart))
  }
  if (page) {
    summarizerData.push(new SummarizerDataSet([page]))
  }

  const additionalInfos = data.ADDITIONAL_INFO ?? []
  const explanations = data.EXPLANATION ?? []

  if (additionalInfos.length > 0) {
    summarizerData.push(new SummarizerDataSet(additionalInfos))
  }
  if (explanations.length > 0) {
    summarizerData.push(new SummarizerDataSet(explanations))
  }

  return normsMetadataSummarizer(summarizerData, "")
}

export function digitalAnnouncementSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []

  const date = data.DATE?.[0]
    ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
    : undefined

  const firstPart = [
    data.ANNOUNCEMENT_MEDIUM?.[0],
    date,
    data.YEAR?.[0],
    data.EDITION?.[0],
  ].filter((f): f is string => f !== undefined)

  const page = data?.PAGE?.[0]

  if (firstPart.length > 0) {
    if (page) {
      firstPart[firstPart.length - 1] = `${firstPart[firstPart.length - 1]},`
    }
    summarizerData.push(new SummarizerDataSet(firstPart))
  }
  if (page) {
    summarizerData.push(new SummarizerDataSet([page]))
  }

  const areaOfPublication = data?.AREA_OF_PUBLICATION?.[0]
  if (areaOfPublication) {
    summarizerData.push(new SummarizerDataSet([areaOfPublication]))
  }

  const numberOfPublication =
    data?.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0]
  if (numberOfPublication) {
    summarizerData.push(new SummarizerDataSet([numberOfPublication]))
  }

  const additionalInfos = data.ADDITIONAL_INFO ?? []
  const explanations = data.EXPLANATION ?? []

  if (additionalInfos.length > 0) {
    summarizerData.push(new SummarizerDataSet(additionalInfos))
  }
  if (explanations.length > 0) {
    summarizerData.push(new SummarizerDataSet(explanations))
  }

  return normsMetadataSummarizer(summarizerData, "")
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

  if (midSection.length > 0) {
    summarizerData.push(new SummarizerDataSet(midSection))
  }
  if (additionalInfos.length > 0) {
    summarizerData.push(new SummarizerDataSet(additionalInfos))
  }
  if (explanations.length > 0) {
    summarizerData.push(new SummarizerDataSet(explanations))
  }

  return normsMetadataSummarizer(summarizerData, "")
}

export function otherOfficialReferenceSummary(data: Metadata): VNode {
  const summarizerData: SummarizerDataSet[] = []
  const otherOfficialReference = data.OTHER_OFFICIAL_REFERENCE?.[0]

  if (otherOfficialReference) {
    summarizerData.push(new SummarizerDataSet([otherOfficialReference]))
  }

  return normsMetadataSummarizer(summarizerData)
}
