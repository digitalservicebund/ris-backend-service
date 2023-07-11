import { describe, test } from "vitest"
import { normProviderSummarizer } from "@/components/normProvider/summarizer"
import { Metadata } from "@/domain/Norm"

describe("participationSummarizer", () => {
  test("summarizes Participation", () => {
    const data: [Metadata, string][] = [
      [
        {
          ENTITY: ["DEU"],
          DECIDING_BODY: ["BT"],
          RESOLUTION_MAJORITY: [true],
        },
        "DEU | BT | Beschlussfassung mit qualifizierter Mehrheit",
      ],
      [
        {
          ENTITY: ["DEU"],
          DECIDING_BODY: ["BT"],
        },
        "DEU | BT",
      ],
      [
        {
          RESOLUTION_MAJORITY: [true],
        },
        "Beschlussfassung mit qualifizierter Mehrheit",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(normProviderSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(normProviderSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(normProviderSummarizer({ LEAD_JURISDICTION: [] })).toBe("")
  })
})
