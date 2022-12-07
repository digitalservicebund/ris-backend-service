import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import DropdownInput from "@/components/DropdownInput.vue"
import { Court } from "@/domain/documentUnit"
import type { DropdownItem } from "@/domain/types"
import { LookupTableEndpoint } from "@/domain/types"
import dropdownInputService from "@/services/dropdownItemService"

const DROPDOWN_ITEMS: DropdownItem[] = [
  { text: "testItem1", value: "t1" },
  { text: "testItem2", value: "t2" },
  { text: "testItem3", value: "t3" },
]

describe("Dropdown Element", () => {
  const user = userEvent.setup()
  it("Dropdown is closed", () => {
    const { queryByDisplayValue } = render(DropdownInput, {
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
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
      },
    })
    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
    ) as HTMLElement
    await user.click(openDropdownContainer)
    let dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(3)
    await user.keyboard("{escape}")
    dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(0)
  })

  it("Dropdown items should be filtered", async () => {
    const user = userEvent.setup()
    const { container, getByLabelText } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "test label",
        dropdownItems: DROPDOWN_ITEMS,
        isCombobox: true,
      },
    })

    const input = getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem2")
  })

  it("Dropdown items should not be filtered after selection", async () => {
    const user = userEvent.setup()
    const { container, getByLabelText } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "test label",
        dropdownItems: DROPDOWN_ITEMS,
        isCombobox: true,
      },
    })

    const input = getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    await user.click(dropdownItems[0])

    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
    ) as HTMLElement

    await user.click(openDropdownContainer)

    expect(
      container.querySelectorAll(".dropdown-container__dropdown-item")
    ).toHaveLength(3)
  })

  it("Dropdown items should stay filtered after typing without selection", async () => {
    const user = userEvent.setup()
    const { container, getByLabelText } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "test label",
        dropdownItems: DROPDOWN_ITEMS,
        isCombobox: true,
      },
    })

    const input = getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
    ) as HTMLElement

    await user.click(openDropdownContainer)
    await user.click(openDropdownContainer)

    expect(
      container.querySelectorAll(".dropdown-container__dropdown-item")
    ).toHaveLength(1)
  })

  it("Dropdown items shouldn't be filtered if no combobox", async () => {
    const { container } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "testItem1",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
      },
    })
    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
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

  it("Text should be selected when click", async () => {
    const { container } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "testItem1",
        ariaLabel: "",
        dropdownItems: DROPDOWN_ITEMS,
      },
    })
    const inputField = container.querySelector("input") as HTMLInputElement
    expect(inputField).toHaveValue("testItem1")
    await user.click(inputField)
    if (
      inputField.selectionStart !== null &&
      inputField.selectionEnd !== null
    ) {
      expect(
        inputField.value.slice(
          inputField.selectionStart,
          inputField.selectionEnd
        )
      ).toEqual("testItem1")
    }
  })

  it("Dropdown items should show message if no items matched", async () => {
    const user = userEvent.setup()
    const { container, getByLabelText } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "test label",
        dropdownItems: DROPDOWN_ITEMS,
        isCombobox: true,
      },
    })

    const input = getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem10")

    const dropdownItems = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
  })

  it("Dropdown uses endpoint to fetch all DocumentType items", async () => {
    const user = userEvent.setup()
    const dropdownItems: DropdownItem[] = [
      {
        text: "AO - Anordnung",
        value: "Anordnung", // <-- string
      },
    ]
    const fetchSpy = vi
      .spyOn(dropdownInputService, "fetch")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    const { container } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        endpoint: LookupTableEndpoint.documentTypes,
        isCombobox: true,
      },
    })

    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
    ) as HTMLElement
    await user.click(openDropdownContainer)

    const dropdownItemElements = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(
      LookupTableEndpoint.documentTypes,
      undefined
    )
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("AO - Anordnung")
  })

  it("Dropdown uses endpoint to fetch all Court items", async () => {
    const user = userEvent.setup()
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: DropdownItem[] = [
      {
        text: "BGH Karlsruhe",
        value: court, // <-- Court
      },
    ]
    const fetchSpy = vi
      .spyOn(dropdownInputService, "fetch")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    const { container } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "",
        endpoint: LookupTableEndpoint.courts,
        isCombobox: true,
      },
    })

    const openDropdownContainer = container.querySelector(
      ".input-expand-icon"
    ) as HTMLElement
    await user.click(openDropdownContainer)

    const dropdownItemElements = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(LookupTableEndpoint.courts, undefined)
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("Dropdown uses endpoint to fetch Court items based on search string", async () => {
    const user = userEvent.setup()
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: DropdownItem[] = [
      {
        text: "BGH Karlsruhe",
        value: court,
      },
    ]
    const fetchSpy = vi
      .spyOn(dropdownInputService, "fetch")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    const { container, getByLabelText } = render(DropdownInput, {
      props: {
        id: "dropdown-test",
        modelValue: "",
        ariaLabel: "test label",
        endpoint: LookupTableEndpoint.courts,
        isCombobox: true,
      },
    })

    const input = getByLabelText("test label") as HTMLInputElement
    await user.type(input, "bgh")

    const dropdownItemElements = container.querySelectorAll(
      ".dropdown-container__dropdown-item"
    )

    expect(fetchSpy).toHaveBeenCalledTimes(3)
    // TODO checking for "b", "bg", "bgh" as the three arguments does not work though
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })
})
