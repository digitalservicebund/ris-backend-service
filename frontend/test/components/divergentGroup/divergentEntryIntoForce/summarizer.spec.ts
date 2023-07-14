import { describe, test } from "vitest"
import { divergentEntryIntoForceSummarizer } from "@/components/divergentGroup/divergentEntryIntoForce/summarizer"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("Official Reference Summaries", () => {
  test.todo("summarizes Divergent Entry Into Force Defined", () => {
    // const data: [MetadataSections, string[]][] = [
    //   [
    //     {
    //       DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
    //         {
    //           DATE: ["01.01.2020"],
    //           [MetadatumType.NORM_CATEGORY]: [
    //             NormCategory.BASE_NORM,
    //             NormCategory.AMENDMENT_NORM,
    //             NormCategory.TRANSITIONAL_NORM,
    //           ],
    //         },
    //       ],
    //     },
    //     ["01.01.2020", "|", "Stammnorm", "Änderungsnorm", "Übergangsnorm"],
    //   ],
    //   [
    //     {
    //       DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
    //         {
    //           [MetadatumType.NORM_CATEGORY]: [
    //             NormCategory.BASE_NORM,
    //             NormCategory.TRANSITIONAL_NORM,
    //           ],
    //         },
    //       ],
    //     },
    //     ["Stammnorm", "Übergangsnorm"],
    //   ],
    // ]
    //
    // data.forEach(([input, expected]) => {
    //   const summmaryLine: VNode = divergentEntryIntoForceSummarizer(input)
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

  test.todo("summarizes Divergent Entry Into Force Undefined", () => {
    // const data: [MetadataSections, string[]][] = [
    //   [
    //     {
    //       DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
    //         {
    //           [MetadatumType.UNDEFINED_DATE]: [UndefinedDate.UNDEFINED_UNKNOWN],
    //           [MetadatumType.NORM_CATEGORY]: [
    //             NormCategory.BASE_NORM,
    //             NormCategory.AMENDMENT_NORM,
    //             NormCategory.TRANSITIONAL_NORM,
    //           ],
    //         },
    //       ],
    //     },
    //     [
    //       "unbestimmt (unbekannt)",
    //       "|",
    //       "Stammnorm",
    //       "Änderungsnorm",
    //       "Übergangsnorm",
    //     ],
    //   ],
    //   [
    //     {
    //       DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
    //         {
    //           [MetadatumType.NORM_CATEGORY]: [NormCategory.BASE_NORM],
    //         },
    //       ],
    //     },
    //     ["Stammnorm"],
    //   ],
    // ]
    //
    // data.forEach(([input, expected]) => {
    //   const summmaryLine: VNode = divergentEntryIntoForceSummarizer(input)
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
    expect(divergentEntryIntoForceSummarizer(undefined).children).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(
      divergentEntryIntoForceSummarizer({ CITATION_DATE: [] }).children,
    ).toBe("")
  })
})
