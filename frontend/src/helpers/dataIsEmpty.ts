export function dataIsEmpty(data?: unknown): boolean {
  switch (typeof data) {
    case "object":
      return Object.values(data ?? {}).every(dataIsEmpty)
    case "string":
      return data.trim().length <= 0
    case "number":
      return false
    default:
      return !data
  }
}
