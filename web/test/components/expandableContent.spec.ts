import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import ExpandableContentnt from "@/components/ExpandableContent.vue"

function renderComponent(options?: {
  header?: string
  isExpanded?: boolean
  defaultSlot?: string
  headerSlot?: string
}) {
  const vuetify = createVuetify()
  const global = { plugins: [vuetify] }
  const slots = {
    default: options?.defaultSlot ?? "",
    header: options?.headerSlot ?? "",
  }
  const props = { header: options?.header, isExpanded: options?.isExpanded }
  const renderResult = render(ExpandableContentnt, { global, slots, props })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

describe("ExpandableContent", () => {
  it("displays given header property as regular text", () => {
    const { queryByText } = renderComponent({ header: "test header" })
    const header = queryByText("test header")

    expect(header).toBeInTheDocument()
  })

  it("renders given header slot", () => {
    const { queryByText } = renderComponent({
      headerSlot: "<span>test header</span>",
    })
    const header = queryByText("test header")

    expect(header).toBeInTheDocument()
  })

  it("renders default slot into content", () => {
    const { getByText } = renderComponent({ defaultSlot: "test content" })
    const content = getByText("test content")

    expect(content).toBeInTheDocument()
  })

  it("hides content per default", () => {
    const { getByText } = renderComponent({ defaultSlot: "test content" })
    const content = getByText("test content")

    expect(content).not.toBeVisible()
  })

  it("can open content per default with property", () => {
    const { getByText } = renderComponent({
      isExpanded: true,
      defaultSlot: "test content",
    })
    const content = getByText("test content")

    expect(content).toBeVisible()
  })

  it("body can be toggled by clicking", async () => {
    const { getByText, getByRole, user } = renderComponent({
      defaultSlot: "test content",
    })
    const header = getByRole("button")
    let content = getByText("test content")

    expect(content).not.toBeVisible()
    await user.click(header)
    content = getByText("test content")
    expect(content).toBeVisible()
    await user.click(header)
    content = getByText("test content")
    expect(content).not.toBeVisible()
  })

  it("emits update event when content gets toggled", async () => {
    const { emitted, getByRole, user } = renderComponent()
    const header = getByRole("button")

    await user.click(header)
    await user.click(header)
    await user.click(header)

    expect(emitted()["update:isExpanded"]).toHaveLength(3)
    expect(emitted()["update:isExpanded"]).toEqual([[true], [false], [true]])
  })
})
