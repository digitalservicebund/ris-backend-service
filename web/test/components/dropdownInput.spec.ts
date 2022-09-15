import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import DropdownInput from "@/components/DropdownInput.vue"
import type { DropdownItem } from "@/domain/types"

const DROPDOWN_ITEMS: DropdownItem[] = [
  { text: "testItem1", value: "t1" },
  { text: "testItem2", value: "t2" },
  { text: "testItem3", value: "t3" },
]

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
        dropdownItems: DROPDOWN_ITEMS,
      },
    })
    const item1 = queryByDisplayValue(DROPDOWN_ITEMS[0].text)
    const item2 = queryByDisplayValue(DROPDOWN_ITEMS[1].text)
    const item3 = queryByDisplayValue(DROPDOWN_ITEMS[2].text)
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
        dropdownItems: DROPDOWN_ITEMS,
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
    expect(item1).toHaveTextContent(DROPDOWN_ITEMS[0].text)
    expect(item2).toHaveTextContent(DROPDOWN_ITEMS[1].text)
    expect(item3).toHaveTextContent(DROPDOWN_ITEMS[2].text)
  })

  it("Close dropdown", async () => {
    const { container, queryByDisplayValue } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
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
    const item1 = queryByDisplayValue(DROPDOWN_ITEMS[0].text)
    const item2 = queryByDisplayValue(DROPDOWN_ITEMS[1].text)
    const item3 = queryByDisplayValue(DROPDOWN_ITEMS[2].text)
    expect(item1).not.toBeInTheDocument()
    expect(item2).not.toBeInTheDocument()
    expect(item3).not.toBeInTheDocument()
  })

  it("Dropdown items should be filted", async () => {
    const { container } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "testItem1",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
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
    expect(item1).toHaveTextContent(DROPDOWN_ITEMS[0].text)
  })

  it("Dropdown items should be show all items if not matched", async () => {
    const { container } = render(DropdownInput, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        modelValue: "testItem4",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
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
    expect(item1).toHaveTextContent(DROPDOWN_ITEMS[0].text)
    expect(item2).toHaveTextContent(DROPDOWN_ITEMS[1].text)
    expect(item3).toHaveTextContent(DROPDOWN_ITEMS[2].text)
  })
})
