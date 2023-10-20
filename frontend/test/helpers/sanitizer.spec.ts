import {
  sanitizeNormTitle,
  sanitizeTableOfContentEntry,
} from "@/helpers/sanitizer"

describe("sanitizer", () => {
  describe("sanitize table of content entry", () => {
    it("replaces lowercase br tags with a space", () => {
      const result = sanitizeTableOfContentEntry("Heading<br/> of section")
      expect(result).toBe("Heading of section")
    })

    it("replaces uppercase tags with a space", () => {
      const result = sanitizeTableOfContentEntry("Heading<BR/> of section")
      expect(result).toBe("Heading of section")
    })

    it("replaces multiple br tags in the same string", () => {
      const result = sanitizeTableOfContentEntry("Heading<br/> of<br/> section")
      expect(result).toBe("Heading of section")
    })

    it("collapses surrounding whitespace", () => {
      const result = sanitizeTableOfContentEntry("Heading <br/>  of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <kw/> tags", () => {
      const result = sanitizeTableOfContentEntry("Heading <kw/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <KW/> tags", () => {
      const result = sanitizeTableOfContentEntry("Heading <KW/>of section")
      expect(result).toBe("Heading of section")
    })

    it('removes <FnR ID="..."/> tags', () => {
      const result = sanitizeTableOfContentEntry(
        'Heading <FnR ID="123"/>of section',
      )
      expect(result).toBe("Heading of section")
    })

    it("removes <b/> tags", () => {
      const result = sanitizeTableOfContentEntry("Heading <b/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <B/> tags", () => {
      const result = sanitizeTableOfContentEntry("Heading <B/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <sup>...</sup> tags", () => {
      const result = sanitizeTableOfContentEntry(
        "Heading <sup>of section</sup>",
      )
      expect(result).toBe("Heading")
    })

    it("removes <SUP>...</SUP> tags", () => {
      const result = sanitizeTableOfContentEntry(
        "Heading <SUP>of section</SUP>",
      )
      expect(result).toBe("Heading")
    })
  })

  describe("sanitize norm title", () => {
    it("replaces lowercase br tags with a linebreak", () => {
      const result = sanitizeNormTitle("Heading<br/> of section")
      expect(result).toBe("Heading\nof section")
    })

    it("replaces uppercase tags with a linebreak", () => {
      const result = sanitizeNormTitle("Heading<BR/> of section")
      expect(result).toBe("Heading\nof section")
    })

    it("replaces multiple br tags in the same string", () => {
      const result = sanitizeNormTitle("Heading<br/> of<br/> section")
      expect(result).toBe("Heading\nof\nsection")
    })

    it("collapses surrounding whitespace", () => {
      const result = sanitizeNormTitle("Heading <br/>  of section")
      expect(result).toBe("Heading\nof section")
    })

    it("removes <kw/> tags", () => {
      const result = sanitizeNormTitle("Heading <kw/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <KW/> tags", () => {
      const result = sanitizeNormTitle("Heading <KW/>of section")
      expect(result).toBe("Heading of section")
    })

    it('removes <FnR ID="..."/> tags', () => {
      const result = sanitizeNormTitle('Heading <FnR ID="123"/>of section')
      expect(result).toBe("Heading of section")
    })

    it("removes <b/> tags", () => {
      const result = sanitizeNormTitle("Heading <b/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <B/> tags", () => {
      const result = sanitizeNormTitle("Heading <B/>of section")
      expect(result).toBe("Heading of section")
    })

    it("removes <sup>...</sup> tags", () => {
      const result = sanitizeNormTitle("Heading <sup>of section</sup>")
      expect(result).toBe("Heading")
    })

    it("removes <SUP>...</SUP> tags", () => {
      const result = sanitizeNormTitle("Heading <SUP>of section</SUP>")
      expect(result).toBe("Heading")
    })
  })
})
