import dayjs from "dayjs"
import { Metadata, UndefinedDate } from "@/domain/norm"

export function getLabel(value: UndefinedDate): string {
  switch (value) {
    case UndefinedDate.UNDEFINED_UNKNOWN:
      return "unbestimmt (unbekannt)"
    case UndefinedDate.UNDEFINED_FUTURE:
      return "unbestimmt (zukünftig)"
    case UndefinedDate.UNDEFINED_NOT_PRESENT:
      return "nicht vorhanden"
    default:
      return ""
  }
}
export function generalSummarizer(data: Metadata): string {
  if (!data) return ""

  const undefinedDate = data?.UNDEFINED_DATE?.[0]

  if (undefinedDate) {
    return getLabel(undefinedDate)
  } else {
    const date = data.DATE?.[0]
      ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
      : undefined
    return date ?? ""
  }
}
