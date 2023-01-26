/* eslint-disable jest-dom/prefer-in-document */
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { Court } from "@/domain/documentUnit"
import { ComboboxItem, ComboboxAttributes } from "@/domain/types"
import service from "@/services/comboboxItemService"

function renderComponent(
  options: {
    id?: string
    modelValue?: string
    itemService?: ComboboxAttributes["itemService"]
    ariaLabel?: string
  } = {}
) {
  return render(ComboboxInput, {
    props: {
      id: options.id ?? "combobox-test",
      modelValue: options.modelValue,
      ariaLabel: options.ariaLabel ?? "test label",
      itemService:
        options.itemService ??
        service.filterItems([
          { label: "testItem1", value: "t1" },
          { label: "testItem2", value: "t2" },
          { label: "testItem3", value: "t3" },
        ]),
    },
  })
}

describe("Combobox Element", () => {
  const user = userEvent.setup()

  it("is closed", () => {
    renderComponent()

    expect(screen.queryByDisplayValue("testItem1")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem2")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("testItem3")).not.toBeInTheDocument()
  })

  it("is opened", async () => {
    renderComponent()

    const openComboboxContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openComboboxContainer)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    await user.keyboard("{escape}")
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
  })

  it("items should be filtered", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)

    expect(dropdownItems[0]).toHaveTextContent("testItem2")
  })

  it("items should stay filtered after selection", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    await user.click(dropdownItems[0])

    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen"
    ) as HTMLElement

    await user.click(openDropdownContainer)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("items should stay filtered after typing without selection", async () => {
    renderComponent()

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

  it("items shouldn't be filtered if no combobox", async () => {
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

  it("items should show message if no items matched", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem10")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
  })

  it("Dropdown item should be visible after selecting", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label") as HTMLInputElement
    await user.type(input, "testItem2")

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem2")

    await user.click(dropdownItems[0])
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    expect(input).toHaveValue("testItem2")

    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen"
    ) as HTMLElement
    await user.click(openDropdownContainer)
    // the filter is still set to "testItem2", so we expect only that one item
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("uses endpoint to fetch all DocumentType items", async () => {
    const fetchSpy = vi
      .spyOn(service, "getDocumentTypes")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: [
            {
              label: "AO - Anordnung",
              value: "Anordnung", // <-- string
            },
          ],
        })
      )

    renderComponent({
      itemService: service.getDocumentTypes,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(undefined)
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("AO - Anordnung")
  })

  it("uses endpoint to fetch all Court items", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: ComboboxItem[] = [
      {
        label: "BGH Karlsruhe",
        value: court, // <-- Court
      },
    ]
    const fetchSpy = vi
      .spyOn(service, "getCourts")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    renderComponent({
      itemService: service.getCourts,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")

    await user.click(openDropdownContainer)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(undefined)
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("Dropdown uses endpoint to fetch Court items based on search string", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: ComboboxItem[] = [
      {
        label: "BGH Karlsruhe",
        value: court,
      },
    ]
    const fetchSpy = vi
      .spyOn(service, "getCourts")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    renderComponent({
      itemService: service.getCourts,
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
    const dropdownItems: ComboboxItem[] = [
      {
        label: "ABC",
        value: court,
      },
    ]
    const fetchSpy = vi
      .spyOn(service, "getCourts")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: dropdownItems })
      )

    renderComponent({
      itemService: service.getCourts,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")
    expect(fetchSpy).toHaveBeenCalledTimes(1)
    expect(fetchSpy).toHaveBeenCalledWith(undefined)
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
