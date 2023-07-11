import { describe, test } from "vitest"
import { summarizeAnnouncementDate } from "@/components/announcementDate/summarizer"
import { Metadata } from "@/domain/Norm"

describe("Announcement Date Summarizer", () => {
  test("summarizes the Announcement Date and Time", () => {
    const data: [Metadata, string][] = [
      [
        {
          DATE: ["01.01.2020"],
          TIME: ["16:36"],
        },
        "01.01.2020 16:36",
      ],
      [
        {
          DATE: ["01.01.2020"],
        },
        "01.01.2020",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeAnnouncementDate(input)).toBe(expected)
    })
  })

  test("summarizes the Announcement Year", () => {
    const data: [Metadata, string][] = [
      [
        {
          YEAR: ["2020"],
        },
        "2020",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeAnnouncementDate(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(summarizeAnnouncementDate(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(summarizeAnnouncementDate({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
