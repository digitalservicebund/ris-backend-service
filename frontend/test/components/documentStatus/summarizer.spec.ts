import { describe, test } from "vitest"
import { documentStatusSectionSummarizer } from "@/components/documentStatus/summarizer"
import {
  MetadataSections,
  OtherType,
  ProofIndication,
  ProofType,
} from "@/domain/Norm"

describe("Document Status Section Summarizer", () => {
  test("summarizes Document Status", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DOCUMENT_STATUS: [
            {
              WORK_NOTE: ["PRÄTEXT(2) BGBl I 2014, 610"],
              DESCRIPTION: ["BGBl I 2014, 610"],
              YEAR: ["2014"],
              REFERENCE: ["reference"],
              ENTRY_INTO_FORCE_DATE_NOTE: ["date note one, date note two"],
              PROOF_INDICATION: [ProofIndication.CONSIDERED],
            },
          ],
        },
        "PRÄTEXT(2) BGBl I 2014, 610 BGBl I 2014, 610 2014 reference date note one, date note two ist berücksichtigt",
      ],
      [
        {
          DOCUMENT_STATUS: [
            {
              WORK_NOTE: ["PRÄTEXT(2) BGBl I 2014, 610"],
              DESCRIPTION: ["BGBl I 2014, 610"],
              YEAR: ["2014"],
              REFERENCE: ["reference"],
              ENTRY_INTO_FORCE_DATE_NOTE: ["date note one, date note two"],
              PROOF_INDICATION: [ProofIndication.NOT_YET_CONSIDERED],
            },
          ],
        },
        "PRÄTEXT(2) BGBl I 2014, 610 BGBl I 2014, 610 2014 reference date note one, date note two noch nicht berücksichtigt",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(documentStatusSectionSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Document Text Proof", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DOCUMENT_TEXT_PROOF: [
            {
              PROOF_TYPE: [ProofType.TEXT_PROOF_FROM],
              TEXT: ["26.6.2017"],
            },
          ],
        },
        "Textnachweis ab 26.6.2017",
      ],
      [
        {
          DOCUMENT_TEXT_PROOF: [
            {
              PROOF_TYPE: [ProofType.TEXT_PROOF_VALIDITY_FROM],
              TEXT: ["26.6.2017"],
            },
          ],
        },
        "Textnachweis Geltung ab 26.6.2017",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(documentStatusSectionSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Document Other", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DOCUMENT_OTHER: [
            {
              OTHER_TYPE: [OtherType.TEXT_IN_PROGRESS],
            },
          ],
        },
        "Text in Bearbeitung",
      ],
      [
        {
          DOCUMENT_OTHER: [
            {
              OTHER_TYPE: [OtherType.TEXT_PROOFED_BUT_NOT_DONE],
            },
          ],
        },
        "Nachgewiesener Text dokumentarisch noch nicht abschließend bearbeitet",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(documentStatusSectionSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(documentStatusSectionSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(documentStatusSectionSummarizer({ CITATION_DATE: [] })).toBe("")
  })
})
