import { describe, it, expect } from "vitest"
import { Norm, Article, Paragraph } from "../../src/domain/Norm"

describe("Norm", () => {
  it("instantiates with a guid and longTitle", () => {
    const articleMock = [
      {
        guid: "123",
        title: "title",
        marker: "(1)",
        paragraphs: [{ guid: "123", marker: "(1)", text: "text" }],
      },
    ]
    const norm = new Norm(
      "longtitle",
      "123",
      articleMock,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      false,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      ""
    )
    expect(norm.longTitle).toEqual("longtitle")
    expect(norm.guid).toEqual("123")
  })

  it("returns Article as object", () => {
    const articleMock = [
      {
        guid: "123",
        title: "title",
        marker: "(1)",
        paragraphs: [{ guid: "123", marker: "(1)", text: "text" }],
      },
    ]
    const norm = new Norm(
      "longtitle",
      "123",
      articleMock,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      false,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      ""
    )

    norm.articles[0].guid = "123"
    norm.articles[0].title = "articleTitle"
    norm.articles[0].marker = "(1)"

    const articles: Article = norm.articles[0]
    expect(articles.guid).toBe("123")
    expect(articles.title).toBe("articleTitle")
    expect(articles.marker).toBe("(1)")
  })

  it("returns Paragraph as object", () => {
    const articleMock = [
      {
        guid: "123",
        title: "title",
        marker: "(1)",
        paragraphs: [{ guid: "123", marker: "(1)", text: "text" }],
      },
    ]
    const norm = new Norm(
      "longtitle",
      "123",
      articleMock,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      false,
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      ""
    )

    norm.articles[0].paragraphs[0].guid = "123"
    norm.articles[0].paragraphs[0].marker = "(1)"
    norm.articles[0].paragraphs[0].text = "articleTitle"

    const paragraphs: Paragraph = norm.articles[0].paragraphs[0]
    expect(paragraphs.guid).toBe("123")
    expect(paragraphs.text).toBe("articleTitle")
    expect(paragraphs.marker).toBe("(1)")
  })
})
