/* eslint-disable jest-dom/prefer-in-document */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DropdownInput from "@/components/DropdownInput.vue"
import { DropdownItem } from "@/domain/types"

function renderComponent(
  options: {
    id?: string
    modelValue?: string
    value?: string
    ariaLabel?: string
    items?: DropdownItem[]
  } = {}
) {
  return render(DropdownInput, {
    props: {
      id: options.id ?? "dropdown-test",
      modelValue: options.modelValue,
      value: options.value,
      ariaLabel: options.ariaLabel ?? "test label",
      items: options.items ?? [
        { label: "testItem1", value: "t1" },
        { label: "testItem2", value: "t2" },
        { label: "testItem3", value: "t3" },
      ],
    },
  })
}

describe("Dropdown Input", () => {
  const user = userEvent.setup()

  it("is closed", async () => {
    renderComponent()

    expect(screen.queryByText("testItem1")).not.toBeInTheDocument()
    expect(screen.queryByText("testItem2")).not.toBeInTheDocument()
    expect(screen.queryByText("testItem3")).not.toBeInTheDocument()
  })

  it("is opened", async () => {
    renderComponent()

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    expect(screen.getByText("testItem1")).toBeInTheDocument()
    expect(screen.getByText("testItem2")).toBeInTheDocument()
    expect(screen.getByText("testItem3")).toBeInTheDocument()

    await user.keyboard("{escape}")
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
  })

  it("displays label for given modelValue", async () => {
    renderComponent({ modelValue: "t3" })
    expect(screen.getByLabelText("test label")).toHaveValue("testItem3")
  })

  it("displays label for given value", async () => {
    renderComponent({ value: "t3" })
    expect(screen.getByLabelText("test label")).toHaveValue("testItem3")
  })

  it("displays selected value after selection", async () => {
    renderComponent()

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    await user.click(screen.getByText("testItem2"))
    expect(screen.getByLabelText("test label")).toHaveValue("testItem2")
  })

  it("emits updated value after selection", async () => {
    const { emitted } = renderComponent({ modelValue: "t3" })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    await user.click(screen.getByText("testItem2"))

    expect(emitted()["update:modelValue"]).toEqual([["t2"]])
  })
})
