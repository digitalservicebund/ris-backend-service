export const instantToDate = (instantStr: string) => {
  return new Date(instantStr).toLocaleDateString("de-DE", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  })
}
