import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import TableOfContents from "@/components/TableOfContents.vue"
import { Article, DocumentSection, DocumentSectionType } from "@/domain/norm"

function renderComponent(options?: {
  documentSections: (Article | DocumentSection)[]
  normGuid?: string
  marginLevel?: number
}) {
  const props = {
    documentSections: options?.documentSections,
    normGuid: options?.normGuid ?? "mockNormGuid",
    marginLevel: options?.marginLevel,
  }
  const utils = render(TableOfContents, {
    props,
    global: {
      plugins: [
        createRouter({
          history: createWebHistory(),
          routes: [
            {
              path: "/norms/norm/:normGuid/documentation/:documentationGuid",
              name: "norms-norm-normGuid-documentation-documentationGuid",
              component: {},
            },
            {
              path: "/",
              name: "root",
              component: {},
            },
          ],
        }),
      ],
    },
  })

  const user = userEvent.setup()
  return { ...utils, user }
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

    const buttons = screen.getAllByLabelText("Zuklappen")
    await user.click(buttons[1])

    expect(
      screen.queryByText("Section Heading of section"),
    ).not.toBeInTheDocument()
  })

  it("it renders with some margin", async () => {
    renderComponent({
      documentSections: [firstPart],
      marginLevel: 2,
    })

    const iconSpan = screen.getByTestId("icons-open-close")

    expect(iconSpan).toHaveClass("ml-[44px]")
  })

  it("generates correct links with parameters", async () => {
    const mockNormGuid = "testNormGuid"

    renderComponent({
      documentSections: [firstPart, secondPart],
      normGuid: mockNormGuid,
    })

    expect(
      screen.getByRole("link", {
        name: `${firstPart.marker} ${firstPart.heading}`,
      }),
    ).toHaveAttribute(
      "href",
      `/norms/norm/${mockNormGuid}/documentation/${firstPart.guid}`,
    )
  })
})
