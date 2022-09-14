import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import DropdownElement from "@/components/DropdownElement.vue"

describe("Dropdown Element", () => {
  const vuetify = createVuetify({ components, directives })
  const user = userEvent.setup()
  it("Dropdown is closed", () => {
    const { queryByDisplayValue } = render(DropdownElement, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        value: "",
        dropdownValues: ["testItem1", "testItem2", "testItem3"],
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
    const { container } = render(DropdownElement, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        value: "",
        dropdownValues: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelectorAll("div")[1]

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
    const { container } = render(DropdownElement, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        value: "testItem1",
        dropdownValues: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelectorAll("div")[1]

    await user.click(openDropdownContainer)
    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(1)
    const item1 = dropdownItems[0]
    expect(item1).toHaveTextContent("testItem1")
  })

  it("Dropdown items should be show all items if not matched", async () => {
    const { container } = render(DropdownElement, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        value: "testItem4",
        dropdownValues: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelectorAll("div")[1]

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

  it("browsing items with arrow key", async () => {
    const { container } = render(DropdownElement, {
      global: { plugins: [vuetify] },
      props: {
        id: "dropdown-test",
        value: "",
        dropdownValues: ["testItem1", "testItem2", "testItem3"],
      },
    })
    const openDropdownContainer = container.querySelectorAll("div")[1]

    await user.click(openDropdownContainer)
    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(3)
    const item1 = dropdownItems[0]
    const item2 = dropdownItems[1]
    const item3 = dropdownItems[2]

    /** Browsing with arrow down */
    await user.keyboard("{arrowdown}")
    expect(item1).toHaveFocus()
    await user.keyboard("{arrowdown}")
    expect(item2).toHaveFocus()
    await user.keyboard("{arrowdown}")
    expect(item3).toHaveFocus()
    await user.keyboard("{arrowdown}")
    expect(item3).toHaveFocus()
    /** Browsing with arrow up */
    await user.keyboard("{arrowup}")
    expect(item2).toHaveFocus()
    await user.keyboard("{arrowup}")
    expect(item1).toHaveFocus()
    await user.keyboard("{arrowup}")
    expect(item1).toHaveFocus()
  })
})
