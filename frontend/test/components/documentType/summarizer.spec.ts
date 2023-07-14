import { describe, test } from "vitest"
import { documentTypeSummarizer } from "@/components/documentType/summarizer"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("Document Type Summarizer", () => {
  test.todo("summarizes the Document Type", () => {
    // const data: [Metadata, string[]][] = [
    //   [
    //     {
    //       TYPE_NAME: ["RV"],
    //       NORM_CATEGORY: [
    //         NormCategory.BASE_NORM,
    //         NormCategory.AMENDMENT_NORM,
    //         NormCategory.TRANSITIONAL_NORM,
    //       ],
    //       TEMPLATE_NAME: ["one", "two"],
    //     },
    //     [
    //       "RV",
    //       "|",
    //       "Stammnorm",
    //       "Änderungsnorm",
    //       "Übergangsnorm",
    //       "|",
    //       "one",
    //       "two",
    //     ],
    //   ],
    //   [
    //     {
    //       NORM_CATEGORY: [NormCategory.TRANSITIONAL_NORM],
    //       TEMPLATE_NAME: ["one", "two"],
    //     },
    //     ["Übergangsnorm", "|", "one", "two"],
    //   ],
    //   [
    //     {
    //       TEMPLATE_NAME: ["one", "two"],
    //     },
    //     ["one", "two"],
    //   ],
    // ]
    //
    // data.forEach(([input, expected]) => {
    //   const summmaryLine: VNode = documentTypeSummarizer(input)
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
    expect(documentTypeSummarizer(undefined).children).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(documentTypeSummarizer({ LEAD_JURISDICTION: [] }).children).toBe("")
  })
})
