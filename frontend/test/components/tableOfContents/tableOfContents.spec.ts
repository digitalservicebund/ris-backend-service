import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import TableOfContents from "@/components/TableOfContents.vue"
import { Article, DocumentSection } from "@/domain/norm"

function renderComponent(options?: {
  documentSections: (Article | DocumentSection)[]
  marginLeft: number
}) {
  const props = {
    documentSections: options?.documentSections,
    marginLeft: options?.marginLeft,
  }
  const utils = render(TableOfContents, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("TableOfContents", () => {
  it("TODO", () => {
    renderComponent({ documentSections: [], marginLeft: 0 })
  })

  it("renders correctly with empty documentation", () => {})

  it("renders document sections correctly", () => {})

  it("renders articles correctly", () => {})

  it("toggles expandable content", async () => {})
})
