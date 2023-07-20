import { describe, test } from "vitest"
import { VNode, VNodeArrayChildren } from "vue"
import {
  normsMetadataSummarizer,
  SummarizerDataSet,
  Type,
} from "@/helpers/normsMetadataSummarizer"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("Norms Metadata Summarizer", () => {
  test("summarizes the metadata passed of type string, date, chip and checkmark", () => {
    const data: [SummarizerDataSet[], string[]][] = [
      [
        [
          new SummarizerDataSet(["string"]),
          new SummarizerDataSet(["2020-01-01"], {
            type: Type.DATE,
            format: "DD.MM.YYYY",
          }),
        ],
        ["string", "|", "01.01.2020"],
      ],
      [
        [
          new SummarizerDataSet(["simple string1", "simple string2"], {
            separator: ",",
          }),
          new SummarizerDataSet(["checkmark1", "checkmark2", "checkmark3"], {
            type: Type.CHECKMARK,
          }),
          new SummarizerDataSet(["chip1", "chip2"], { type: Type.CHIP }),
        ],
        [
          "simple string1,",
          "simple string2",
          "|",
          "checkmark1",
          "checkmark2",
          "checkmark3",
          "|",
          "chip1",
          "chip2",
        ],
      ],
    ]

    data.forEach(([input, expected]) => {
      const summmaryLineWithoutSeparator: VNode = normsMetadataSummarizer(
        input,
        "",
      )
      expect(summmaryLineWithoutSeparator.children).not.toBeNull()
      const childrenWithoutSeparator = summmaryLineWithoutSeparator!.children!
      const countValues = input
        .map((m) => m.value.length)
        .reduce((a, b) => a + b, 0)
      expect(childrenWithoutSeparator.length).toBe(countValues)

      const summmaryLine: VNode = normsMetadataSummarizer(input)
      expect(summmaryLine.children).not.toBeNull()

      const children = summmaryLine!.children!
      expect(children.length).toBe(expected.length)

      for (const [key, value] of Object.entries(children)) {
        const content = (value as VNode).children!
        expect(content).not.toBeNull()
        if (typeof content === "string") {
          expect(content).toBe(expected[Number(key)])
        }
        if (typeof content === "object") {
          const element = (content as VNodeArrayChildren)[1]
          expect(element).not.toBeNull()
          expect((element! as VNode).children).toBe(expected[Number(key)])
        }
      }
    })
  })

  test("summarizes the metadata passed of type bold in line and footnote", () => {
    const data: [SummarizerDataSet[], ([string, string] | string)[]][] = [
      [
        [
          new SummarizerDataSet(["footnote reference"], {
            type: Type.BOLD_IN_ONE_LINE,
          }),
          new SummarizerDataSet(["type1", "footnote1"], {
            type: Type.FOOTNOTE,
          }),
          new SummarizerDataSet(["type2", "footnote2"], {
            type: Type.FOOTNOTE,
          }),
          new SummarizerDataSet(["type3", "footnote3"], {
            type: Type.FOOTNOTE,
          }),
        ],
        [
          "footnote reference",
          ["type1", "footnote1"],
          ["type2", "footnote2"],
          ["type3", "footnote3"],
        ],
      ],
    ]

    data.forEach(([input, expected]) => {
      const summmaryLine: VNode = normsMetadataSummarizer(input)
      expect(summmaryLine.children).not.toBeNull()

      const referenceAndFootnoteTypes = summmaryLine!.children!

      const reference = (
        (referenceAndFootnoteTypes as VNodeArrayChildren)[0] as VNode
      ).children
      expect(reference).toBe(expected[0])

      const footNotes = (
        (referenceAndFootnoteTypes as VNodeArrayChildren)[1] as VNode
      ).children as VNodeArrayChildren
      expect(footNotes).not.toBeNull()
      expect(footNotes!.length).toBe(expected.length - 1)

      for (let i = 1; i <= footNotes!.length; i++) {
        const content = (Object.values(footNotes)[i - 1] as VNode).children

        const element0 = (content as VNodeArrayChildren)[0]
        expect(element0).not.toBeNull()
        expect((element0! as VNode).children).toBe(expected[i][0])

        const element1 = (content as VNodeArrayChildren)[1]
        expect(element1).not.toBeNull()
        expect((element1! as VNode).children).toBe(expected[i][1])
      }
    })
  })
})
