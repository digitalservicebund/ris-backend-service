import dayjs from "dayjs"
import { Metadata, MetadataSections } from "@/domain/Norm"

export function officialReferenceSummarizer(data: MetadataSections): string {
  if (!data) return ""

  if (data.PRINT_ANNOUNCEMENT) {
    return printAnnouncementSummary(data.PRINT_ANNOUNCEMENT[0])
  } else if (data.DIGITAL_ANNOUNCEMENT) {
    return digitalAnnouncementSummary(data.DIGITAL_ANNOUNCEMENT[0])
  } else if (data.EU_ANNOUNCEMENT) {
    return euAnnouncementSummary(data.EU_ANNOUNCEMENT[0])
  } else if (data.OTHER_OFFICIAL_ANNOUNCEMENT) {
    return otherOfficialReferenceSummary(data.OTHER_OFFICIAL_ANNOUNCEMENT[0])
  } else return ""
}
export function printAnnouncementSummary(data: Metadata): string {
  if (data) {
    const midSection = [
      data.ANNOUNCEMENT_GAZETTE?.[0],
      data.YEAR?.[0],
      data.NUMBER?.[0],
      data.PAGE?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Papierverkündungsblatt",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
}

export function digitalAnnouncementSummary(data: Metadata): string {
  if (data) {
    const date = data.DATE?.[0]
      ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
      : undefined

    const midSection = [
      data.ANNOUNCEMENT_MEDIUM?.[0],
      date,
      data.YEAR?.[0],
      data.PAGE?.[0],
      data.EDITION?.[0],
      data.AREA_OF_PUBLICATION?.[0],
      data.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Elektronisches Verkündungsblatt",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
}

export function euAnnouncementSummary(data: Metadata): string {
  if (data) {
    const midSection = [
      data.EU_GOVERNMENT_GAZETTE?.[0],
      data.YEAR?.[0],
      data.SERIES?.[0],
      data.NUMBER?.[0],
      data.PAGE?.[0],
    ]
      .filter(Boolean)
      .join(", ")

    return [
      "Amtsblatt der EU",
      midSection,
      data.ADDITIONAL_INFO?.join(", "),
      data.EXPLANATION?.join(", "),
    ]
      .filter(Boolean)
      .join(" | ")
  } else {
    return ""
  }
}

export function otherOfficialReferenceSummary(data: Metadata): string {
  if (!data) return ""

  const otherOfficialReference = data.OTHER_OFFICIAL_REFERENCE?.[0] ?? ""

  return `Sonstige amtliche Fundstelle | ${otherOfficialReference}`
}
