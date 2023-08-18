import { describe, test } from "vitest"
import { Metadata } from "@/domain/norm"
import { dateYearSummarizer } from "@/helpers/dateYearSummarizer"

describe("Publication Date Summarizer", () => {
  test("summarizes the Publication Date", () => {
    const data: [Metadata, string][] = [
      [
        {
          DATE: ["01.01.2020"],
        },
        "01.01.2020",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(dateYearSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Publication Year", () => {
    const data: [Metadata, string][] = [
      [
        {
          YEAR: ["2020"],
        },
        "2020",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(dateYearSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(dateYearSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(dateYearSummarizer({ ANNOUNCEMENT_MEDIUM: [] })).toBe("")
  })
})
