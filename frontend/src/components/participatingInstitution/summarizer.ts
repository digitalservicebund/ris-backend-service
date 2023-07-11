import { Metadata } from "@/domain/Norm"

export function participationSummarizer(data: Metadata) {
  if (!data) return ""

  const type = data.PARTICIPATION_TYPE?.[0]
  const institution = data.PARTICIPATION_INSTITUTION?.[0]

  return [type, institution]
    .filter((value) => value != "" && value != null)
    .join(" | ")
}
