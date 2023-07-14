import { describe, test } from "vitest"
import { subjectAreaSummarizer } from "@/components/subjectArea/summarizer"
import { Metadata } from "@/domain/Norm"

describe("subjectAreaSummarizer", () => {
  test("summarizes SubjectArea", () => {
    const data: [Metadata, string][] = [
      [
        {
          SUBJECT_FNA: ["315-12"],
          SUBJECT_PREVIOUS_FNA: ["671-34"],
          SUBJECT_GESTA: ["678-90"],
          SUBJECT_BGB_3: ["233-54"],
        },
        "FNA-Nummer 315-12 | Frühere FNA-Nummer 671-34 | GESTA-Nummer 678-90 | Bundesgesetzblatt Teil III 233-54",
      ],
      [
        {
          SUBJECT_PREVIOUS_FNA: ["671-34"],
          SUBJECT_BGB_3: ["233-54"],
        },
        "Frühere FNA-Nummer 671-34 | Bundesgesetzblatt Teil III 233-54",
      ],
      [
        {
          SUBJECT_FNA: ["315-12"],
        },
        "FNA-Nummer 315-12",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(subjectAreaSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(subjectAreaSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(subjectAreaSummarizer({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
