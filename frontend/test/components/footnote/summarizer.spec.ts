import { describe, test } from "vitest"
import { summarizeFootnotePerLine } from "@/components/footnote/summarizer"

/* eslint-disable  @typescript-eslint/no-non-null-assertion */
// Using non-null assertion after manually checking if not null

describe("summarizeFootnotePerLine", () => {
  test.todo("summarizes Footnotes", () => {
    // const data: [Footnote, (string | string[])[]][] = [
    //   [
    //     {
    //       FOOTNOTE: [
    //         {
    //           FOOTNOTE_REFERENCE: ["footnoteReference"],
    //         },
    //         {
    //           FOOTNOTE_CHANGE: ["footnoteChange1"],
    //         },
    //         {
    //           FOOTNOTE_COMMENT: ["footnoteComment"],
    //         },
    //         {
    //           FOOTNOTE_DECISION: ["footnoteDecision"],
    //         },
    //         {
    //           FOOTNOTE_STATE_LAW: ["footnoteStateLaw"],
    //         },
    //         {
    //           FOOTNOTE_EU_LAW: ["footnoteEuLaw"],
    //         },
    //         {
    //           FOOTNOTE_OTHER: ["footnoteOther"],
    //         },
    //         {
    //           FOOTNOTE_CHANGE: ["footnoteChange2"],
    //         },
    //       ],
    //     },
    //     [
    //       "footnoteReference",
    //       ["Änderungsfußnote", "footnoteChange1"],
    //       ["Kommentierende Fußnote", "footnoteComment"],
    //       ["BVerfG-Entscheidung", "footnoteDecision"],
    //       ["Landesrecht", "footnoteStateLaw"],
    //       ["EU/EG-Recht", "footnoteEuLaw"],
    //       ["Sonstige Fußnote", "footnoteOther"],
    //       ["Änderungsfußnote", "footnoteChange2"],
    //     ],
    //   ],
    // ]
    //
    // data.forEach(([input, expected]) => {
    //   const summmaryLine: VNode = summarizeFootnotePerLine(input)
    //   expect(summmaryLine.children).not.toBeNull()
    //
    //   const referenceAndFootnoteTypes = summmaryLine!.children!
    //
    //   const reference = referenceAndFootnoteTypes[0].children
    //   expect(reference).toBe(expected[0])
    //
    //   const footNotes = referenceAndFootnoteTypes[1].children
    //   expect(footNotes.length).toBe(expected.length - 1)
    //
    //   for (let i = 1; i <= footNotes.length; i++) {
    //     const content = Object.values(footNotes)[i - 1].children
    //     expect(content[0].children[0].children).toBe(expected[i][0])
    //     expect(content[0].children[1].children).toBe(expected[i][1])
    //   }
    // })
  })

  test("fails gracefully when no data is provided", () => {
    expect(summarizeFootnotePerLine(undefined).children).toBe("")
  })
})
