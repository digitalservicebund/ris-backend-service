import dayjs from "dayjs"

export default class DateUtil {
  public static formatDate(
    date: string | Date | undefined,
  ): string | undefined {
    const parsedDate = dayjs(date, "YYYY-MM-DD", true)
    return parsedDate.isValid() ? parsedDate.format("DD.MM.YYYY") : undefined
  }

  public static formatDateTime(date: string | Date): string {
    return dayjs(date).format("DD.MM.YYYY [um] HH:mm [Uhr]")
  }
}

/**
 * Parses an ISO date (YYYY-MM-DD) and returns it in local format (DD.MM.YYYY)
 * Returns `null` if the input is invalid
 */
export function parseIsoDateToLocal(isoDate: string): string | null {
  const date = dayjs(isoDate, "YYYY-MM-DD", true)
  if (!date.isValid()) {
    return null
  }
  return date.format("DD.MM.YYYY")
}

/**
 * Parses a local date (DD.MM.YYYY) to an ISO format (YYYY-MM-DD)
 * Returns `null` if the input is invalid
 */
export function parseLocalDateToIso(localDate: string): string | null {
  const date = dayjs(localDate, "DD.MM.YYYY", true)
  if (!date.isValid()) {
    return null
  }
  return date.format("YYYY-MM-DD")
}
