import dayjs from "dayjs"
import { Metadata } from "@/domain/Norm"

export function dateYearSummarizer(data: Metadata): string {
  if (!data) return ""

  if (data.YEAR) {
    return data.YEAR.toString()
  }

  const date = data.DATE?.[0]
    ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
    : undefined
  return date || ""
}
