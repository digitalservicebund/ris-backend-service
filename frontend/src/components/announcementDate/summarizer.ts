import dayjs from "dayjs"
import { Metadata } from "@/domain/Norm"

export function summarizeAnnouncementDate(data: Metadata): string {
  if (!data) return ""

  let output = ""

  if (data.YEAR?.length) {
    output += data.YEAR.toString()
  } else if (data.DATE?.length) {
    output += dayjs(data.DATE[0]).format("DD.MM.YYYY")
    if (data.TIME?.length) {
      output += " " + data.TIME[0]
    }
  }

  return output
}
