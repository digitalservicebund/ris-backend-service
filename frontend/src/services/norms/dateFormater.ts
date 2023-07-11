export function formatDate(
  dateStrings: (string | undefined)[] | undefined
): string {
  const dateString = Array.isArray(dateStrings) ? dateStrings[0] : dateStrings

  if (!dateString) {
    return ""
  }

  const date = new Date(dateString)
  const day = date.getDate().toString().padStart(2, "0")
  const month = (date.getMonth() + 1).toString().padStart(2, "0")
  const year = date.getFullYear().toString()
  return `${day}.${month}.${year}`
}
