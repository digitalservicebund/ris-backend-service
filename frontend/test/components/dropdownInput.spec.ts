/* eslint-disable jest-dom/prefer-in-document */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DropdownInput from "@/components/DropdownInput.vue"
import { DropdownItem } from "@/domain/types"

function renderComponent(
  options: {
    id?: string
    modelValue?: string
    ariaLabel?: string
    items?: DropdownItem[]
  } = {}
) {
  return render(DropdownInput, {
    props: {
      id: options.id ?? "dropdown-test",
      modelValue: options.modelValue,
      ariaLabel: options.ariaLabel ?? "test label",
      items: options.items ?? [
        { text: "testItem1", value: "t1" },
        { text: "testItem2", value: "t2" },
        { text: "testItem3", value: "t3" },
      ],
    },
  })
}

describe("Dropdown Input", () => {
  const user = userEvent.setup()

  it("is closed", () => {
    renderComponent()

    expect(screen.queryByDisplayValue("testItem1")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem2")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem3")).not.toBeInTheDocument()
  })

  it("is opened", async () => {
    renderComponent()

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    await user.keyboard("{escape}")
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
  })
})
