import { describe, test } from "vitest"
import { participationSummarizer } from "@/components/participatingInstitution/summarizer"
import { Metadata } from "@/domain/Norm"

describe("participationSummarizer", () => {
  test("summarizes Participation", () => {
    const data: [Metadata, string][] = [
      [
        {
          PARTICIPATION_TYPE: ["EZ"],
          PARTICIPATION_INSTITUTION: ["BR"],
        },
        "EZ | BR",
      ],
      [
        {
          PARTICIPATION_TYPE: ["EZ"],
        },
        "EZ",
      ],
      [
        {
          PARTICIPATION_INSTITUTION: ["BR"],
        },
        "BR",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(participationSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(participationSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(participationSummarizer({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
