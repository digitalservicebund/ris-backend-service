import { describe, test } from "vitest"
import { documentTypeSummarizer } from "@/components/documentType/summarizer"
import { Metadata, NormCategory } from "@/domain/Norm"

describe("Document Type Summarizer", () => {
  test("summarizes the Document Type", () => {
    const data: [Metadata, string][] = [
      [
        {
          TYPE_NAME: ["RV"],
          NORM_CATEGORY: [
            NormCategory.BASE_NORM,
            NormCategory.TRANSITIONAL_NORM,
            NormCategory.AMENDMENT_NORM,
          ],
          TEMPLATE_NAME: ["one", "two"],
        },
        "RV | Änderungsnorm Stammnorm Übergangsnorm | one, two",
      ],
      [
        {
          TYPE_NAME: ["RV"],
          TEMPLATE_NAME: ["one", "two"],
        },
        "RV | one, two",
      ],
      [
        {
          TEMPLATE_NAME: ["one", "two"],
        },
        "one, two",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(documentTypeSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    expect(documentTypeSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(documentTypeSummarizer({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
