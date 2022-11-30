import { fireEvent, render } from "@testing-library/vue"
import SubField from "@/components/SubField.vue"

function renderComponent({
  isExpanded = false,
  iconExpanding = "add",
  iconClosing = "horizontal_rule",
}: {
  isExpanded?: boolean
  iconExpanding?: string
  iconClosing?: string
} = {}) {
  return render(SubField, {
    props: { isExpanded, iconExpanding, iconClosing },
    slots: { default: "<div>foo slot</div>" },
  })
}

describe("SubField", () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  it("should be hidden by default", () => {
    const { getByText } = renderComponent()
    expect(getByText("foo slot")).not.toBeVisible()
  })

  it("should be expanded if prop says so", () => {
    const { getByText } = renderComponent({ isExpanded: true })
    expect(getByText("foo slot")).toBeVisible()
  })

  it("should close on toggle if open", async () => {
    const { emitted, getByText, getByLabelText } = renderComponent({
      isExpanded: true,
    })
    expect(getByText("foo slot")).toBeVisible()

    await fireEvent.click(getByLabelText("Abweichendes Feld schließen"))
    expect(getByLabelText("Abweichendes Feld öffnen")).toBeVisible()
    expect(emitted()["update:isExpanded"][0]).toEqual([false])
  })

  it("should close on toggle if open", async () => {
    const { emitted, getByText, getByLabelText } = renderComponent({
      isExpanded: false,
    })
    expect(getByText("foo slot")).not.toBeVisible()

    await fireEvent.click(getByLabelText("Abweichendes Feld öffnen"))
    expect(getByLabelText("Abweichendes Feld schließen")).toBeVisible()
    expect(emitted()["update:isExpanded"][0]).toEqual([true])
  })

  it("should render correct icon if closed", () => {
    const { getByText } = renderComponent({
      iconExpanding: "foo icon",
    })

    expect(getByText("foo icon")).toBeVisible()
  })

  it("should render correct icon if open", () => {
    const { getByText } = renderComponent({
      isExpanded: true,
      iconClosing: "foo icon",
    })

    expect(getByText("foo icon")).toBeVisible()
  })
})
