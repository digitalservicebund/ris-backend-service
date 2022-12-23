import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ExpandableContentnt from "@/components/ExpandableContent.vue"

function renderComponent({
  header,
  isExpanded,
  defaultSlot = "",
  headerSlot = "",
}: {
  header?: string
  isExpanded?: boolean
  defaultSlot?: string
  headerSlot?: string
} = {}) {
  const slots = {
    default: defaultSlot,
    header: headerSlot,
  }
  const props = { header, isExpanded }
  const utils = render(ExpandableContentnt, { slots, props })
  const user = userEvent.setup()

  return { user, ...utils }
}

describe("ExpandableContent", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  it("displays given header property as regular text", () => {
    renderComponent({ header: "test header" })
    const header = screen.queryByText("test header")

    expect(header).toBeInTheDocument()
  })

  it("renders given header slot", () => {
    renderComponent({
      headerSlot: "<span>test header</span>",
    })
    const header = screen.queryByText("test header")

    expect(header).toBeInTheDocument()
  })

  it("renders default slot into content", () => {
    renderComponent({ defaultSlot: "test content" })
    const content = screen.getByText("test content")

    expect(content).toBeInTheDocument()
  })

  it("hides content per default", () => {
    renderComponent({ defaultSlot: "test content" })
    const content = screen.getByText("test content")

    expect(content).not.toBeVisible()
  })

  it("can open content per default with property", () => {
    renderComponent({
      isExpanded: true,
      defaultSlot: "test content",
    })
    const content = screen.getByText("test content")

    expect(content).toBeVisible()
  })

  it("body can be toggled by clicking", async () => {
    const { user } = renderComponent({
      defaultSlot: "test content",
    })
    const header = screen.getByRole("button")
    let content = screen.getByText("test content")

    expect(content).not.toBeVisible()
    await user.click(header)
    content = screen.getByText("test content")
    expect(content).toBeVisible()
    await user.click(header)
    content = screen.getByText("test content")
    expect(content).not.toBeVisible()
  })

  it("emits update event when content gets toggled", async () => {
    const { emitted, user } = renderComponent()
    const header = screen.getByRole("button")

    await user.click(header)
    await user.click(header)
    await user.click(header)

    expect(emitted()["update:isExpanded"]).toHaveLength(3)
    expect(emitted()["update:isExpanded"]).toEqual([[true], [false], [true]])
  })
})
