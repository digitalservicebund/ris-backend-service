import { DocumentSectionType } from "@/domain/norm"
import {
  isArticle,
  isDocumentSection,
  type Documentation,
  type DocumentSection,
  type Article,
} from "@/domain/norm"

describe("norm domain", () => {
  describe("article type guard", () => {
    test("returns true for an article", () => {
      const article: Article = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
        text: "text",
      }

      expect(isArticle(article)).toBe(true)
    })

    test("returns false for a document section", () => {
      const documentSection: DocumentSection = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
        type: DocumentSectionType.BOOK,
        documentation: [],
      }

      expect(isArticle(documentSection)).toBe(false)
    })

    test("returns false for a documentation", () => {
      const documentation: Documentation = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
      }

      expect(isArticle(documentation)).toBe(false)
    })
  })

  describe("document section type guard", () => {
    test("returns true for a document section", () => {
      const documentSection: DocumentSection = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
        type: DocumentSectionType.BOOK,
        documentation: [],
      }

      expect(isDocumentSection(documentSection)).toBe(true)
    })

    test("returns false for an article", () => {
      const article: Article = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
        text: "text",
      }

      expect(isDocumentSection(article)).toBe(false)
    })

    test("returns false for a documentation", () => {
      const documentation: Documentation = {
        guid: "guid",
        heading: "heading",
        marker: "marker",
      }

      expect(isDocumentSection(documentation)).toBe(false)
    })
  })
})
