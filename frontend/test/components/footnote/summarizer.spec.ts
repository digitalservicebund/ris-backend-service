import { VNode, VNodeArrayChildren } from "vue"
import { summarizeFootnotePerLine } from "@/components/footnote/summarizer"
import { Footnote } from "@/components/footnote/types"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("summarizeFootnotePerLine", () => {
  test("summarizes the metadata passed of type bold in line and footnote", () => {
    const data: [Footnote, (string | string[])[]][] = [
      [
        {
          FOOTNOTE: [
            {
              FOOTNOTE_REFERENCE: ["footnoteReference"],
            },
            {
              FOOTNOTE_CHANGE: ["footnoteChange1"],
            },
            {
              FOOTNOTE_COMMENT: ["footnoteComment"],
            },
            {
              FOOTNOTE_DECISION: ["footnoteDecision"],
            },
            {
              FOOTNOTE_STATE_LAW: ["footnoteStateLaw"],
            },
            {
              FOOTNOTE_EU_LAW: ["footnoteEuLaw"],
            },
            {
              FOOTNOTE_OTHER: ["footnoteOther"],
            },
            {
              FOOTNOTE_CHANGE: ["footnoteChange2"],
            },
          ],
        },
        [
          "footnoteReference",
          ["Änderungsfußnote", "footnoteChange1"],
          ["Kommentierende Fußnote", "footnoteComment"],
          ["BVerfG-Entscheidung", "footnoteDecision"],
          ["Landesrecht", "footnoteStateLaw"],
          ["EU/EG-Recht", "footnoteEuLaw"],
          ["Sonstige Fußnote", "footnoteOther"],
          ["Änderungsfußnote", "footnoteChange2"],
        ],
      ],
    ]

    data.forEach(([input, expected]) => {
      const summmaryLine: VNode = summarizeFootnotePerLine(input)
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

  test("fails gracefully when no data is provided", () => {
    expect(summarizeFootnotePerLine(undefined).children).toBe("")
  })
})
