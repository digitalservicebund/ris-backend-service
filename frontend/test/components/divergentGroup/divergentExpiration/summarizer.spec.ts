import { describe, test } from "vitest"
import { DivergentExpirationSummarizer } from "@/components/divergentGroup/divergentExpiration/summarizer"
import {
  MetadataSections,
  MetadatumType,
  NormCategory,
  UndefinedDate,
} from "@/domain/Norm"

describe("Divergent Expiration Summaries", () => {
  test("summarizes Divergent Expiration Defined", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DIVERGENT_EXPIRATION_DEFINED: [
            {
              DATE: ["01.01.2020"],
              [MetadatumType.NORM_CATEGORY]: [
                NormCategory.BASE_NORM,
                NormCategory.TRANSITIONAL_NORM,
                NormCategory.AMENDMENT_NORM,
              ],
            },
          ],
        },
        "01.01.2020 | Änderungsnorm Stammnorm Übergangsnorm",
      ],
      [
        {
          DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
            {
              [MetadatumType.NORM_CATEGORY]: [
                NormCategory.BASE_NORM,
                NormCategory.TRANSITIONAL_NORM,
                NormCategory.AMENDMENT_NORM,
              ],
            },
          ],
        },
        "Änderungsnorm Stammnorm Übergangsnorm",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(DivergentExpirationSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Divergent Expiration Undefined", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DIVERGENT_EXPIRATION_UNDEFINED: [
            {
              [MetadatumType.UNDEFINED_DATE]: [UndefinedDate.UNDEFINED_UNKNOWN],
              [MetadatumType.NORM_CATEGORY]: [
                NormCategory.BASE_NORM,
                NormCategory.TRANSITIONAL_NORM,
                NormCategory.AMENDMENT_NORM,
              ],
            },
          ],
        },
        "unbestimmt (unbekannt) | Änderungsnorm Stammnorm Übergangsnorm",
      ],
      [
        {
          DIVERGENT_EXPIRATION_UNDEFINED: [
            {
              [MetadatumType.UNDEFINED_DATE]: [UndefinedDate.UNDEFINED_FUTURE],
              [MetadatumType.NORM_CATEGORY]: [NormCategory.BASE_NORM],
            },
          ],
        },
        "unbestimmt (zukünftig) | Änderungsnorm",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(DivergentExpirationSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(DivergentExpirationSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(DivergentExpirationSummarizer({ CITATION_DATE: [] })).toBe("")
  })
})
