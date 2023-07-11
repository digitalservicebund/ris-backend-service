import { describe, test } from "vitest"
import { officialReferenceSummarizer } from "@/components/officialReference/summarizer"
import { MetadataSections } from "@/domain/Norm"

describe("Official Reference Summaries", () => {
  test("summarizes Print Announcement", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          PRINT_ANNOUNCEMENT: [
            {
              ANNOUNCEMENT_GAZETTE: ["BGBl I"],
              YEAR: ["2023"],
              NUMBER: ["67"],
              PAGE: ["3"],
              ADDITIONAL_INFO: ["additional info"],
              EXPLANATION: ["explanation"],
            },
          ],
        },
        "Papierverkündungsblatt | BGBl I, 2023, 67, 3 | additional info | explanation",
      ],
      [
        {
          PRINT_ANNOUNCEMENT: [
            {
              ANNOUNCEMENT_GAZETTE: ["BGBl I"],
              YEAR: ["2023"],
              NUMBER: ["67"],
              PAGE: ["3"],
              ADDITIONAL_INFO: ["one", "two", "three"],
              EXPLANATION: ["explanation", "another explanation"],
            },
          ],
        },
        "Papierverkündungsblatt | BGBl I, 2023, 67, 3 | one, two, three | explanation, another explanation",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(officialReferenceSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Digital Announcement", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          DIGITAL_ANNOUNCEMENT: [
            {
              ANNOUNCEMENT_MEDIUM: ["BGBl I"],
              DATE: ["10.10.2023"],
              YEAR: ["2023"],
              PAGE: ["one"],
              EDITION: ["3"],
              AREA_OF_PUBLICATION: ["area"],
              NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: [
                "publication number",
              ],
              ADDITIONAL_INFO: ["info one", "info two"],
              EXPLANATION: ["explanation one", "explanation two"],
            },
          ],
        },
        "Elektronisches Verkündungsblatt | BGBl I, 10.10.2023, 2023, one, 3, area, publication number | info one, info two | explanation one, explanation two",
      ],
      [
        {
          DIGITAL_ANNOUNCEMENT: [
            {
              ANNOUNCEMENT_MEDIUM: ["BGBl I"],
              DATE: ["10.10.2023"],
              YEAR: ["2023"],
              PAGE: ["one"],
              EDITION: ["3"],
              AREA_OF_PUBLICATION: ["area"],
              NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: [
                "publication number",
              ],
            },
          ],
        },
        "Elektronisches Verkündungsblatt | BGBl I, 10.10.2023, 2023, one, 3, area, publication number",
      ],
      [
        {
          DIGITAL_ANNOUNCEMENT: [
            {
              ANNOUNCEMENT_MEDIUM: ["BGBl I"],
              DATE: ["10.10.2023"],
              YEAR: ["2023"],
              PAGE: ["one"],
              EDITION: ["3"],
              AREA_OF_PUBLICATION: ["area"],
              ADDITIONAL_INFO: ["info one", "info two"],
              EXPLANATION: ["explanation one", "explanation two"],
            },
          ],
        },
        "Elektronisches Verkündungsblatt | BGBl I, 10.10.2023, 2023, one, 3, area | info one, info two | explanation one, explanation two",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(officialReferenceSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Eu Announcement", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          EU_ANNOUNCEMENT: [
            {
              EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
              YEAR: ["2023"],
              SERIES: ["100"],
              NUMBER: ["67"],
              PAGE: ["3"],
              ADDITIONAL_INFO: ["info one", "info two"],
              EXPLANATION: ["explanation one", "explanation two"],
            },
          ],
        },
        "Amtsblatt der EU | Amtsblatt der EU, 2023, 100, 67, 3 | info one, info two | explanation one, explanation two",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(officialReferenceSummarizer(input)).toBe(expected)
    })
  })

  test("summarizes Other Official Announcement", () => {
    const data: [MetadataSections, string][] = [
      [
        {
          OTHER_OFFICIAL_ANNOUNCEMENT: [
            {
              OTHER_OFFICIAL_REFERENCE: ["text"],
            },
          ],
        },
        "Sonstige amtliche Fundstelle | text",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(officialReferenceSummarizer(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(officialReferenceSummarizer(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(officialReferenceSummarizer({ CITATION_DATE: [] })).toBe("")
  })
})
