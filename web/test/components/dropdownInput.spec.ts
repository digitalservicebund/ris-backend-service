import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import DropdownInput from "@/components/DropdownInput.vue"

describe("Dropdown Element", () => {
  const vuetify = createVuetify({ components, directives })
  const user = userEvent.setup()
  it("Dropdown is closed", () => {
    const { queryByDisplayValue } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        dropdownItems: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const item1 = queryByDisplayValue("testItem1")
    const item2 = queryByDisplayValue("testItem2")
    const item3 = queryByDisplayValue("testItem3")
    expect(item1).not.toBeInTheDocument()
    expect(item2).not.toBeInTheDocument()
    expect(item3).not.toBeInTheDocument()
  })

  it("Dropdown is opened", async () => {
    const { container } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        dropdownItems: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelector(
      ".toggle-dropdown-button"
    ) as HTMLElement
    await user.click(openDropdownContainer)
    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(3)
    const item1 = dropdownItems[0]
    const item2 = dropdownItems[1]
    const item3 = dropdownItems[2]
    expect(item1).toHaveTextContent("testItem1")
    expect(item2).toHaveTextContent("testItem2")
    expect(item3).toHaveTextContent("testItem3")
  })

  it("Dropdown items should be filted", async () => {
    const { container } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "testItem1",
        ariaLabel: "",
        dropdownItems: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelector(
      ".toggle-dropdown-button"
    ) as HTMLElement

    await user.click(openDropdownContainer)
    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(1)
    const item1 = dropdownItems[0]
    expect(item1).toHaveTextContent("testItem1")
  })

  it("Dropdown items should be show all items if not matched", async () => {
    const { container } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "testItem4",
        ariaLabel: "",
        dropdownItems: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelector(
      ".toggle-dropdown-button"
    ) as HTMLElement

    await user.click(openDropdownContainer)
    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(3)
    const item1 = dropdownItems[0]
    const item2 = dropdownItems[1]
    const item3 = dropdownItems[2]
    expect(item1).toHaveTextContent("testItem1")
    expect(item2).toHaveTextContent("testItem2")
    expect(item3).toHaveTextContent("testItem3")
  })
})
