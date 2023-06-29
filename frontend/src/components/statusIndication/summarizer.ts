import dayjs from "dayjs"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"

function join(...data: (string | string[])[]): string {
  return data
    .filter((x) => (Array.isArray(x) ? x.length > 0 : x !== ""))
    .map((x) => (Array.isArray(x) ? x.join(", ") : x))
    .join(" | ")
}

function summarizeUpdate(
  type: MetadataSectionName.STATUS | MetadataSectionName.REISSUE,
  data: Metadata
): string {
  const typeName = type === MetadataSectionName.STATUS ? "Stand" : "Neufassung"

  const note = data.NOTE ? data.NOTE[0] : ""
  const reference = data.REFERENCE ? data.REFERENCE : []

  let descriptionOrArticle = ""
  if (type === MetadataSectionName.STATUS) {
    descriptionOrArticle = data.DESCRIPTION ? data.DESCRIPTION[0] : ""
  } else if (type === MetadataSectionName.REISSUE) {
    descriptionOrArticle = data.ARTICLE ? data.ARTICLE[0] : ""
  }

  let date = data.DATE ? data.DATE[0] : ""
  if (date) date = dayjs(date).format("DD.MM.YYYY")

  return join(typeName, note, descriptionOrArticle, date, reference)
}

export function summarizeStatusIndication(data: MetadataSections): string {
  if (!data) {
    return ""
  } else if (data.STATUS) {
    return summarizeUpdate(MetadataSectionName.STATUS, data.STATUS[0])
  } else if (data.REISSUE) {
    return summarizeUpdate(MetadataSectionName.REISSUE, data.REISSUE[0])
  } else if (data.REPEAL) {
    return join("Aufhebung", data.REPEAL?.[0]?.TEXT?.[0] ?? "")
  } else if (data.OTHER_STATUS) {
    return join("Sonstiger Hinweis", data.OTHER_STATUS?.[0]?.NOTE?.[0] ?? "")
  } else {
    return ""
  }
}
