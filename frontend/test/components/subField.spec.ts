import { fireEvent, render, screen } from "@testing-library/vue"
import SubField from "@/components/SubField.vue"

function renderComponent({
  ariaLabel = "Test Feld",
  isExpanded = false,
  iconExpanding = "add",
  iconClosing = "horizontal_rule",
}: {
  ariaLabel?: string
  isExpanded?: boolean
  iconExpanding?: string
  iconClosing?: string
} = {}) {
  return render(SubField, {
    props: { ariaLabel, isExpanded, iconExpanding, iconClosing },
    slots: { default: "<div>foo slot</div>" },
  })
}

describe("SubField", () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  it("should be hidden by default", () => {
    renderComponent()
    expect(screen.getByText("foo slot")).not.toBeVisible()
  })

  it("should be expanded if prop says so", () => {
    renderComponent({ isExpanded: true })
    expect(screen.getByText("foo slot")).toBeVisible()
  })

  it("should close on toggle if open", async () => {
    const { emitted } = renderComponent({
      isExpanded: true,
    })
    expect(screen.getByText("foo slot")).toBeVisible()

    await fireEvent.click(screen.getByLabelText("Test Feld schließen"))
    expect(screen.getByLabelText("Test Feld anzeigen")).toBeVisible()
    expect(emitted()["update:isExpanded"][0]).toEqual([false])
  })

  it("should open on toggle if close", async () => {
    const { emitted } = renderComponent({
      isExpanded: false,
    })
    expect(screen.getByText("foo slot")).not.toBeVisible()

    await fireEvent.click(screen.getByLabelText("Test Feld anzeigen"))
    expect(screen.getByLabelText("Test Feld schließen")).toBeVisible()
    expect(emitted()["update:isExpanded"][0]).toEqual([true])
  })

  it("should render dynamic aria label", async () => {
    const { emitted } = renderComponent({
      ariaLabel: "Abweichendes Feld",
    })
    expect(screen.getByText("foo slot")).not.toBeVisible()

    await fireEvent.click(screen.getByLabelText("Abweichendes Feld anzeigen"))
    expect(screen.getByLabelText("Abweichendes Feld schließen")).toBeVisible()
    await fireEvent.click(screen.getByLabelText("Abweichendes Feld schließen"))
    expect(screen.getByLabelText("Abweichendes Feld anzeigen")).toBeVisible()
    expect(emitted()["update:isExpanded"][0]).toEqual([true])
  })

  it("should render correct icon if closed", () => {
    renderComponent({
      iconExpanding: "foo icon",
    })

    expect(screen.getByText("foo icon")).toBeVisible()
  })

  it("should render correct icon if open", () => {
    renderComponent({
      isExpanded: true,
      iconClosing: "foo icon",
    })

    expect(screen.getByText("foo icon")).toBeVisible()
  })
})
