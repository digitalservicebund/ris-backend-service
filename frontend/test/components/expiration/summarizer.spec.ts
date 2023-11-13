import { Metadata, UndefinedDate } from "@/domain/norm"
import { generalSummarizer } from "@/helpers/generalSummarizer"

describe("Expiration date Summarizer", () => {
  test("summarizes the Date", () => {
    const data: [Metadata, string][] = [
      [
        {
          DATE: ["01.01.2020"],
        },
        "01.01.2020",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(generalSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes the Undefined date", () => {
    const data: [Metadata, string][] = [
      [
        {
          UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN],
        },
        "unbestimmt (unbekannt)",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(generalSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(generalSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(generalSummarizer({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
