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
        ],
        [
          "simple string1,",
          "simple string2",
          "|",
          "checkmark1",
          "checkmark2",
          "checkmark3",
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
})
