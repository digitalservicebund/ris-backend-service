import { describe, test } from "vitest"
import { normProviderSummarizer } from "@/components/normProvider/summarizer"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("participationSummarizer", () => {
  test.todo("summarizes Participation", () => {
    // const data: [Metadata, string[]][] = [
    //   [
    //     {
    //       ENTITY: ["DEU"],
    //       DECIDING_BODY: ["BT"],
    //       RESOLUTION_MAJORITY: [true],
    //     },
    //     ["DEU", "|", "BT", "|", "Beschlussfassung mit qual. Mehrheit"],
    //   ],
    //   [
    //     {
    //       DECIDING_BODY: ["BT"],
    //       RESOLUTION_MAJORITY: [true],
    //     },
    //     ["BT", "|", "Beschlussfassung mit qual. Mehrheit"],
    //   ],
    //   [
    //     {
    //       RESOLUTION_MAJORITY: [true],
    //     },
    //     ["Beschlussfassung mit qual. Mehrheit"],
    //   ],
    // ]
    //
    // data.forEach(([input, expected]) => {
    //   const summmaryLine: VNode = normProviderSummarizer(input)
    //   expect(summmaryLine.children).not.toBeNull()
    //
    //   const children = summmaryLine!.children!
    //   expect(children.length).toBe(expected.length)
    //
    //   for (const [key, value] of Object.entries(children)) {
    //     const content = value.children
    //     if (typeof content === "string") {
    //       expect(content).toBe(expected[Number(key)])
    //     }
    //     if (typeof content === "object") {
    //       expect(content[1].children).toBe(expected[Number(key)])
    //     }
    //   }
    // })
  })

  test("fails gracefully when no data is provided", () => {
    expect(normProviderSummarizer(undefined).children).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(normProviderSummarizer({ LEAD_JURISDICTION: [] }).children).toBe("")
  })
})
