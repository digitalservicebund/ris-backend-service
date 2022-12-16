/* eslint-disable jest-dom/prefer-in-document */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DropdownInput from "@/components/DropdownInput.vue"
import { Court } from "@/domain/documentUnit"
import { DropdownItem, LookupTableEndpoint } from "@/domain/types"
import dropdownInputService from "@/services/dropdownItemService"

function renderComponent(
  options: {
    id?: string
    modelValue?: string
    ariaLabel?: string
    dropdownItems?: DropdownItem[]
    isCombobox?: boolean
    preselectedValue?: string
    endpoint?: LookupTableEndpoint
  } = {}
) {
  return render(DropdownInput, {
    props: {
      id: options.id ?? "dropdown-test",
      modelValue: options.modelValue,
      ariaLabel: options.ariaLabel ?? "test label",
      dropdownItems: options.dropdownItems ?? [
        { text: "testItem1", value: "t1" },
        { text: "testItem2", value: "t2" },
        { text: "testItem3", value: "t3" },
      ],
      isCombobox: options.isCombobox ?? false,
      preselectedValue: options.preselectedValue,
      endpoint: options.endpoint,
    },
  })
}

describe("Dropdown Element", () => {
  const user = userEvent.setup()

  it("Dropdown is closed", () => {
    renderComponent()

    expect(screen.queryByDisplayValue("testItem1")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem2")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem3")).not.toBeInTheDocument()
  })

  it("Dropdown is opened", async () => {
    renderComponent()

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    await user.keyboard("{escape}")
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
  })

  it("Dropdown items should be filtered", async () => {
    renderComponent({ isCombobox: true })

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)

    expect(dropdownItems[0]).toHaveTextContent("testItem2")
  })

  it("Dropdown items should not be filtered after selection", async () => {
    renderComponent({ isCombobox: true })

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    await user.click(dropdownItems[0])

    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen"
    ) as HTMLElement

    await user.click(openDropdownContainer)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
  })

  it("Dropdown items should stay filtered after typing without selection", async () => {
    renderComponent({ isCombobox: true })

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    const closeDropdownContainer = screen.getByLabelText(
      "Dropdown schließen"
    ) as HTMLElement

    await user.click(closeDropdownContainer)
    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen"
    ) as HTMLElement
    await user.click(openDropdownContainer)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("Dropdown items shouldn't be filtered if no combobox", async () => {
    renderComponent({ modelValue: "testItem1" })

    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen"
    ) as HTMLElement

    await user.click(openDropdownContainer)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(3)
    expect(dropdownItems[0]).toHaveTextContent("testItem1")
    expect(dropdownItems[1]).toHaveTextContent("testItem2")
    expect(dropdownItems[2]).toHaveTextContent("testItem3")
  })

  it("Dropdown items should show message if no items matched", async () => {
    renderComponent({ isCombobox: true })

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem10")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
  })

  it("Dropdown uses endpoint to fetch all DocumentType items", async () => {
    const fetchSpy = vi
      .spyOn(dropdownInputService, "fetch")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: [
            {
              text: "AO - Anordnung",
              value: "Anordnung", // <-- string
            },
          ],
        })
      )

    renderComponent({
      endpoint: LookupTableEndpoint.documentTypes,
      isCombobox: true,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(
      LookupTableEndpoint.documentTypes,
      undefined
    )
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("AO - Anordnung")
  })

  it("Dropdown uses endpoint to fetch all Court items", async () => {
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

    renderComponent({
      endpoint: LookupTableEndpoint.courts,
      isCombobox: true,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")

    await user.click(openDropdownContainer)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(LookupTableEndpoint.courts, undefined)
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("Dropdown uses endpoint to fetch Court items based on search string", async () => {
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

    renderComponent({
      endpoint: LookupTableEndpoint.courts,
      isCombobox: true,
    })

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "bgh")

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")

    expect(fetchSpy).toHaveBeenCalledTimes(3)
    // TODO checking for "b", "bg", "bgh" as the three arguments does not work though
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("Court without location and with revoked string gets displayed correctly", async () => {
    const court: Court = {
      type: "ABC",
      location: "",
      label: "ABC",
      revoked: "aufgehoben seit: 1973",
    }
    const dropdownItems: DropdownItem[] = [
      {
        text: "ABC",
        value: court,
      },
    ]
    const fetchSpy = vi
      .spyOn(dropdownInputService, "fetch")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    renderComponent({
      endpoint: LookupTableEndpoint.courts,
      isCombobox: true,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")
    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(LookupTableEndpoint.courts, undefined)
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent(
      "ABC aufgehoben seit: 1973"
    )

    const additionalInfoElement = screen.getAllByLabelText(
      "additional-dropdown-info"
    )
    expect(additionalInfoElement.length).toBe(1)
    expect(additionalInfoElement[0]).toHaveTextContent("aufgehoben seit: 1973")
  })
})
