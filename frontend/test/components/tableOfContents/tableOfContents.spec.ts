import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import TableOfContents from "@/components/TableOfContents.vue"
import { Article, DocumentSection, DocumentSectionType } from "@/domain/norm"

function renderComponent(options?: {
  documentSections: (Article | DocumentSection)[]
  marginLeft?: number
}) {
  const props = {
    documentSections: options?.documentSections,
    marginLeft: options?.marginLeft,
  }
  const utils = render(TableOfContents, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

const firstPart: DocumentSection = {
  guid: "guid",
  marker: "First Part",
  heading: "Heading of first part",
  type: DocumentSectionType.PART,
}

const secondPart: DocumentSection = {
  guid: "guid",
  marker: "Second Part",
  heading: "Heading of second part",
  type: DocumentSectionType.PART,
  documentation: [
    {
      guid: "guid",
      marker: "Section",
      heading: "Heading of section",
      type: DocumentSectionType.SECTION,
      documentation: [
        {
          guid: "guid",
          marker: "Subsection",
          heading: "Heading of subsection",
          type: DocumentSectionType.SUBSECTION,
          documentation: [
            {
              guid: "guid",
              marker: "§ 1",
              heading: "Article heading",
              paragraphs: [],
            },
          ],
        },
      ],
    },
  ],
}

describe("TableOfContents", () => {
  it("renders with only articles", () => {
    const firstArticle: Article = {
      guid: "guid",
      marker: "§ 1",
      heading: "First article heading",
      paragraphs: [],
    }

    const secondArticle: Article = {
      guid: "guid",
      marker: "§ 2",
      heading: "Second article heading",
      paragraphs: [],
    }

    renderComponent({
      documentSections: [firstArticle, secondArticle],
    })

    const firstArticleRendered = screen.getByText("§ 1 First article heading")
    expect(firstArticleRendered).toBeInTheDocument()

    const secondArticleRendered = screen.getByText("§ 2 Second article heading")
    expect(secondArticleRendered).toBeInTheDocument()

    const buttons = screen.queryAllByRole("button")
    expect(buttons.length).toBe(0)
  })

  it("renders with sections and article, all open", () => {
    renderComponent({
      documentSections: [firstPart, secondPart],
    })

    const firstPartRendered = screen.getByText(
      "First Part Heading of first part",
    )
    expect(firstPartRendered).toBeInTheDocument()

    const secondPartRendered = screen.getByText(
      "Second Part Heading of second part",
    )
    expect(secondPartRendered).toBeInTheDocument()

    const section = screen.getByText("Section Heading of section")
    expect(section).toBeInTheDocument()

    const subsection = screen.getByText("Subsection Heading of subsection")
    expect(subsection).toBeInTheDocument()

    const article = screen.getByText("§ 1 Article heading")
    expect(article).toBeInTheDocument()

    const buttons = screen.queryAllByRole("button")
    expect(buttons.length).toBe(4)
  })

  it("closing parent node hides all children", async () => {
    const { user } = renderComponent({
      documentSections: [firstPart, secondPart],
    })

    const firstPartRendered = screen.getByText(
      "Second Part Heading of second part",
    )
    await user.click(firstPartRendered)

    const section = screen.queryByText("Section Heading of section")
    expect(section).not.toBeInTheDocument()

    const subsection = screen.queryByText("Subsection Heading of subsection")
    expect(subsection).not.toBeInTheDocument()

    const article = screen.queryByText("§ 1 Article heading")
    expect(article).not.toBeInTheDocument()
  })

  it("it renders with some margin", async () => {
    renderComponent({
      documentSections: [firstPart],
      marginLeft: 20,
    })

    const iconSpan = screen.getByLabelText("Zuklappen")

    expect(iconSpan).toHaveClass("ml-[20px]")
  })
})
