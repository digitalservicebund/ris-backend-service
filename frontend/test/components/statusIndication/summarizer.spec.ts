import { describe, test } from "vitest"
import { summarizeStatusIndication } from "@/components/statusIndication/summarizer"
import { MetadataSections } from "@/domain/Norm"

describe("summarizeStatusIndication", () => {
  test("summarizes status", () => {
    const data: [MetadataSections, string][] = [
      [{ STATUS: [{}] }, "Stand"],
      [
        {
          STATUS: [
            {
              NOTE: ["note"],
              REFERENCE: ["one", "two"],
              DATE: ["2023-05-12"],
              DESCRIPTION: ["description"],
            },
          ],
        },
        "Stand | note | description | 12.05.2023 | one, two",
      ],
      [
        {
          STATUS: [
            {
              NOTE: ["note"],
              REFERENCE: ["one"],
              DATE: ["2023-05-12"],
              DESCRIPTION: ["description"],
            },
          ],
        },
        "Stand | note | description | 12.05.2023 | one",
      ],
      [
        {
          STATUS: [
            {
              NOTE: ["note"],
              REFERENCE: undefined,
              DATE: ["2023-05-12"],
              DESCRIPTION: ["description"],
            },
          ],
        },
        "Stand | note | description | 12.05.2023",
      ],
      [
        {
          STATUS: [
            {
              NOTE: ["note"],
              REFERENCE: ["one", "two"],
              DATE: undefined,
              DESCRIPTION: ["description"],
            },
          ],
        },
        "Stand | note | description | one, two",
      ],
      [
        {
          STATUS: [
            {
              NOTE: ["note"],
              REFERENCE: ["one", "two"],
              DATE: ["2023-05-12"],
              DESCRIPTION: undefined,
            },
          ],
        },
        "Stand | note | 12.05.2023 | one, two",
      ],
      [
        {
          STATUS: [
            {
              NOTE: undefined,
              REFERENCE: ["one", "two"],
              DATE: ["2023-05-12"],
              DESCRIPTION: ["description"],
            },
          ],
        },
        "Stand | description | 12.05.2023 | one, two",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeStatusIndication(input)).toBe(expected)
    })
  })

  test("summarizes reissue", () => {
    const data: [MetadataSections, string][] = [
      [{ REISSUE: [{}] }, "Neufassung"],
      [
        {
          REISSUE: [
            {
              NOTE: ["note"],
              REFERENCE: ["one"],
              DATE: ["2023-05-12"],
              ARTICLE: ["article"],
            },
          ],
        },
        "Neufassung | note | article | 12.05.2023 | one",
      ],
      [
        {
          REISSUE: [
            {
              NOTE: ["note"],
              REFERENCE: undefined,
              DATE: ["2023-05-12"],
              ARTICLE: ["article"],
            },
          ],
        },
        "Neufassung | note | article | 12.05.2023",
      ],
      [
        {
          REISSUE: [
            {
              NOTE: ["note"],
              REFERENCE: ["one"],
              DATE: undefined,
              ARTICLE: ["article"],
            },
          ],
        },
        "Neufassung | note | article | one",
      ],
      [
        {
          REISSUE: [
            {
              NOTE: ["note"],
              REFERENCE: ["one"],
              DATE: ["2023-05-12"],
              ARTICLE: undefined,
            },
          ],
        },
        "Neufassung | note | 12.05.2023 | one",
      ],
      [
        {
          REISSUE: [
            {
              NOTE: undefined,
              REFERENCE: ["one"],
              DATE: ["2023-05-12"],
              ARTICLE: ["article"],
            },
          ],
        },
        "Neufassung | article | 12.05.2023 | one",
      ],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeStatusIndication(input)).toBe(expected)
    })
  })

  test("summarizes repeal", () => {
    const data: [MetadataSections, string][] = [
      [{ REPEAL: [] }, "Aufhebung"],
      [{ REPEAL: [{ TEXT: ["text"] }] }, "Aufhebung | text"],
      [{ REPEAL: [{ TEXT: [] }] }, "Aufhebung"],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeStatusIndication(input)).toBe(expected)
    })
  })

  test("summarizes other status", () => {
    const data: [MetadataSections, string][] = [
      [{ OTHER_STATUS: [] }, "Sonstiger Hinweis"],
      [{ OTHER_STATUS: [{ NOTE: ["note"] }] }, "Sonstiger Hinweis | note"],
      [{ OTHER_STATUS: [{ NOTE: [] }] }, "Sonstiger Hinweis"],
    ]

    data.forEach(([input, expected]) => {
      expect(summarizeStatusIndication(input)).toBe(expected)
    })
  })

  test("fails gracefully when no data is provided", () => {
    // @ts-expect-error Not allowed by TypeScript but just to make sure
    expect(summarizeStatusIndication(undefined)).toBe("")
  })

  test("fails gracefully when trying to summarize unsupported data", () => {
    expect(summarizeStatusIndication({ CITATION_DATE: [] })).toBe("")
  })
})
