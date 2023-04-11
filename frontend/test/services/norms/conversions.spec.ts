import {
  MetadatumType,
  MetadataSectionName,
  MetadataSections,
} from "@/domain/Norm"
import {
  decodeMetadataSections,
  encodeMetadataSections,
} from "@/services/norms/conversions"
import { MetadataSectionSchema } from "@/services/norms/schemas"

describe("conversions", () => {
  describe("decodeMetadataSections()", () => {
    it("groups sections based on their name", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 0,
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
        },
        {
          name: MetadataSectionName.LEAD,
          order: 1,
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [{}, {}],
        PARTICIPATION: [{}],
      })
    })

    it("sorts sections with the same name based on their order property", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 1,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "second", order: 0 },
          ],
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
          metadata: [
            {
              type: MetadatumType.PARTICIPATION_TYPE,
              value: "first",
              order: 0,
            },
          ],
        },
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "first", order: 0 },
          ],
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [{ LEAD_UNIT: ["first"] }, { LEAD_UNIT: ["second"] }],
        PARTICIPATION: [{ PARTICIPATION_TYPE: ["first"] }],
      })
    })

    it("groups metadata based on the type", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            {
              type: MetadatumType.LEAD_UNIT,
              value: "text",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "text",
              order: 1,
            },
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 0,
            },
          ],
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [{ LEAD_UNIT: ["text", "text"], LEAD_JURISDICTION: ["text"] }],
      })
    })

    // Find balance between all possible type + names and individual decoding.
    it("decodes metadata values based on the type property", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            {
              type: MetadatumType.LEAD_UNIT,
              value: "text",
              order: 0,
            },
          ],
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [{ LEAD_UNIT: ["text"] }],
      })
    })

    it("sorts metadata of same type based on their order property", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            {
              type: MetadatumType.LEAD_UNIT,
              value: "second",
              order: 1,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "first",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 2,
            },
          ],
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [{ LEAD_UNIT: ["first", "second"], LEAD_JURISDICTION: ["text"] }],
      })
    })

    it("recursively decodes complex data structures", () => {
      const encoded: MetadataSectionSchema[] = [
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            {
              type: MetadatumType.LEAD_UNIT,
              value: "second",
              order: 1,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "first",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 0,
            },
          ],
        },
        {
          name: MetadataSectionName.LEAD,
          order: 1,
          metadata: [
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "text",
              order: 0,
            },
          ],
          sections: [
            {
              name: MetadataSectionName.PARTICIPATION,
              order: 0,
              metadata: [
                {
                  type: MetadatumType.PARTICIPATION_INSTITUTION,
                  value: "second",
                  order: 1,
                },
                {
                  type: MetadatumType.PARTICIPATION_TYPE,
                  value: "text",
                  order: 0,
                },
                {
                  type: MetadatumType.PARTICIPATION_INSTITUTION,
                  value: "first",
                  order: 0,
                },
              ],
            },
          ],
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
          metadata: [
            {
              type: MetadatumType.PARTICIPATION_TYPE,
              value: "first",
              order: 0,
            },
            {
              type: MetadatumType.PARTICIPATION_TYPE,
              value: "second",
              order: 1,
            },
            {
              type: MetadatumType.PARTICIPATION_INSTITUTION,
              value: "text",
              order: 0,
            },
          ],
        },
      ]

      const decoded = decodeMetadataSections(encoded)

      expect(decoded).toStrictEqual({
        LEAD: [
          {
            LEAD_UNIT: ["first", "second"],
            LEAD_JURISDICTION: ["text"],
          },
          {
            LEAD_JURISDICTION: ["text"],
            LEAD_UNIT: ["text"],
            PARTICIPATION: [
              {
                PARTICIPATION_INSTITUTION: ["first", "second"],
                PARTICIPATION_TYPE: ["text"],
              },
            ],
          },
        ],
        PARTICIPATION: [
          {
            PARTICIPATION_TYPE: ["first", "second"],
            PARTICIPATION_INSTITUTION: ["text"],
          },
        ],
      })
    })
  })

  describe("encodeMetadataSections()", () => {
    it("maps empty section list to null", () => {
      const decoded: MetadataSections = {}

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toBeNull()
    })

    it("maps empty child sections list to null", () => {
      const decoded: MetadataSections = { LEAD: [{ LEAD_UNIT: [] }] }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [],
          sections: null,
        },
      ])
    })

    it("maps section names based on group key", () => {
      const decoded: MetadataSections = {
        LEAD: [{}],
        PARTICIPATION: [{}],
      }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [],
          sections: null,
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
          metadata: [],
          sections: null,
        },
      ])
    })

    it("sets section order property based on their collection index", () => {
      const decoded: MetadataSections = {
        LEAD: [{ LEAD_UNIT: ["first"] }, { LEAD_UNIT: ["second"] }],
        PARTICIPATION: [{ PARTICIPATION_TYPE: ["first"] }],
      }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "first", order: 0 },
          ],
          sections: null,
        },
        {
          name: MetadataSectionName.LEAD,
          order: 1,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "second", order: 0 },
          ],
          sections: null,
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
          metadata: [
            {
              type: MetadatumType.PARTICIPATION_TYPE,
              value: "first",
              order: 0,
            },
          ],
          sections: null,
        },
      ])
    })

    // Find balance between all possible type + names and individual decoding.
    it("encodes metadata values based on their group key used as type property", () => {
      const decoded: MetadataSections = {
        LEAD: [{ LEAD_UNIT: ["text"] }],
      }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "text", order: 0 },
          ],
          sections: null,
        },
      ])
    })

    it("sets metadata order property based on their collection index", () => {
      const decoded: MetadataSections = {
        LEAD: [
          {
            LEAD_UNIT: ["first", "second"],
            LEAD_JURISDICTION: ["text"],
          },
        ],
      }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          metadata: [
            { type: MetadatumType.LEAD_UNIT, value: "first", order: 0 },
            { type: MetadatumType.LEAD_UNIT, value: "second", order: 1 },
            { type: MetadatumType.LEAD_JURISDICTION, value: "text", order: 0 },
          ],
          sections: null,
        },
      ])
    })

    it("recursively encodes complex data structures", () => {
      const decoded: MetadataSections = {
        LEAD: [
          {
            LEAD_UNIT: ["first", "second"],
            LEAD_JURISDICTION: ["text"],
          },
          {
            LEAD_JURISDICTION: ["text"],
            LEAD_UNIT: ["text"],
            PARTICIPATION: [
              {
                PARTICIPATION_TYPE: ["first", "second"],
                PARTICIPATION_INSTITUTION: ["text"],
              },
            ],
          },
        ],
        PARTICIPATION: [
          {
            PARTICIPATION_TYPE: ["text"],
            PARTICIPATION_INSTITUTION: ["first", "second"],
          },
        ],
      }

      const encoded = encodeMetadataSections(decoded)

      expect(encoded).toStrictEqual([
        {
          name: MetadataSectionName.LEAD,
          order: 0,
          sections: null,
          metadata: [
            {
              type: MetadatumType.LEAD_UNIT,
              value: "first",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "second",
              order: 1,
            },
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 0,
            },
          ],
        },
        {
          name: MetadataSectionName.LEAD,
          order: 1,
          metadata: [
            {
              type: MetadatumType.LEAD_JURISDICTION,
              value: "text",
              order: 0,
            },
            {
              type: MetadatumType.LEAD_UNIT,
              value: "text",
              order: 0,
            },
          ],
          sections: [
            {
              name: MetadataSectionName.PARTICIPATION,
              order: 0,
              sections: null,
              metadata: [
                {
                  type: MetadatumType.PARTICIPATION_TYPE,
                  value: "first",
                  order: 0,
                },
                {
                  type: MetadatumType.PARTICIPATION_TYPE,
                  value: "second",
                  order: 1,
                },
                {
                  type: MetadatumType.PARTICIPATION_INSTITUTION,
                  value: "text",
                  order: 0,
                },
              ],
            },
          ],
        },
        {
          name: MetadataSectionName.PARTICIPATION,
          order: 0,
          sections: null,
          metadata: [
            {
              type: MetadatumType.PARTICIPATION_TYPE,
              value: "text",
              order: 0,
            },
            {
              type: MetadatumType.PARTICIPATION_INSTITUTION,
              value: "first",
              order: 0,
            },
            {
              type: MetadatumType.PARTICIPATION_INSTITUTION,
              value: "second",
              order: 1,
            },
          ],
        },
      ])
    })
  })

  test("metadata type and section names have no name intersection", () => {
    for (const name in MetadataSectionName) {
      expect(Object.values(MetadatumType)).not.includes(name)
    }
  })
})
