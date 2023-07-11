import { Metadata } from "@/domain/Norm"

export function subjectAreaSummarizer(data: Metadata) {
  if (!data) return ""

  const fna = data.SUBJECT_FNA?.[0]
  const previousFna = data.SUBJECT_PREVIOUS_FNA?.[0]
  const gesta = data.SUBJECT_GESTA?.[0]
  const bgb3 = data.SUBJECT_BGB_3?.[0]

  return [
    fna && `FNA-Nummer ${fna}`,
    previousFna && `Frühere FNA-Nummer ${previousFna}`,
    gesta && `GESTA-Nummer ${gesta}`,
    bgb3 && `Bundesgesetzblatt Teil III ${bgb3}`,
  ]
    .filter(Boolean)
    .join(" | ")
}
