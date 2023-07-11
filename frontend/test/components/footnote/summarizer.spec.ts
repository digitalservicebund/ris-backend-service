import { describe, test } from "vitest"
import { summarizeFootnotePerLine } from "@/components/footnote/summarizer"
import { MetadataSections } from "@/domain/Norm"

describe("summarizeFootnotePerLine", () => {
  test("summarizes Footnotes", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          FOOTNOTES: [
            {
              FOOTNOTE_REFERENCE: ["note"],
              FOOTNOTE_CHANGE: ["one", "two"],
              FOOTNOTE_COMMENT: ["2023-05-12"],
              FOOTNOTE_DECISION: ["description"],
              FOOTNOTE_STATE_LAW: ["description"],
              FOOTNOTE_EU_LAW: ["description"],
              FOOTNOTE_OTHER: ["description"],
            },
          ],
        },
        "Stand | note | description | 12.05.2023 | one, two",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeFootnotePerLine(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(summarizeFootnotePerLine(undefined)).toBe("")
  })

  // test("fails gracefully when trying to summarize unsupported data", () => {
  //   expect(summarizeFootnotePerLine({ CITATION_DATE: [] })).toBe("")
  // })
})
