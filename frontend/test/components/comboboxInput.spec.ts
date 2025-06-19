import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { ComboboxAttributes, ComboboxItem } from "@/components/input/types"
import { Court } from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"

const items = [
  {
    label: "courtlabel1",
    type: "courttype1",
    location: "courtlocation1",
  },
  {
    label: "courtlabel2",
    type: "courttype2",
    location: "courtlocation2",
  },
  {
    label: "courtlabel3",
    type: "courttype3",
    location: "courtlocation3",
  },
]

const server = setupServer(
  http.get("/api/v1/caselaw/courts", ({ request }) => {
    const filter = new URL(request.url).searchParams.get("q")
    const filteredItems = filter
      ? items.filter((item) => item.label.includes(filter))
      : items
    return HttpResponse.json(filteredItems)
  }),
)

function renderComponent(
  options: {
    id?: string
    modelValue?: ComboboxItem
    itemService?: ComboboxAttributes["itemService"]
    ariaLabel?: string
    manualEntry?: boolean
    noClear?: boolean
  } = {},
) {
  return {
    ...render(ComboboxInput, {
      props: {
        id: options.id ?? "combobox-test",
        modelValue: options.modelValue,
        ariaLabel: options.ariaLabel ?? "test label",
        manualEntry: options.manualEntry ?? false,
        noClear: options.noClear ?? false,
        itemService: options.itemService ?? comboboxItemService.getCourts,
      },
    }),
  }
}

const debounceTimeout = 200

describe("Combobox Element", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() =>
    vi.useFakeTimers({
      toFake: ["setTimeout", "clearTimeout", "Date"],
    }),
  )
  afterEach(() => {
    server.resetHandlers()
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })
  const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })

  it("is closed", () => {
    renderComponent()

    expect(screen.queryByDisplayValue("courtlabel1")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("courtlabel1")).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue("courtlabel1")).not.toBeInTheDocument()
  })

  it("is opened", async () => {
    renderComponent()

    const openComboboxContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openComboboxContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    const input = screen.getByLabelText("test label")
    await fireEvent.focus(input)
    await user.keyboard("{escape}")
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
  })

  it("focus should open dropdown", async () => {
    renderComponent()
    const input = screen.getByLabelText("test label")
    await fireEvent.focus(input)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
  })

  it("enter should select top value", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("test label")
    await fireEvent.focus(input)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)

    input.focus()
    await user.keyboard("{enter}")

    await vi.advanceTimersByTimeAsync(debounceTimeout)

    expect(emitted()["update:modelValue"]).toEqual([
      [
        {
          type: "courttype1",
          location: "courtlocation1",
          label: "courtlabel1",
        },
      ],
    ])
  })

  it("clear-button should unset the currently set value", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("test label")

    await user.type(input, "court")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    await user.click(dropdownItems[1])

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()

    const resetButton = screen.getByLabelText("Auswahl zurücksetzen")
    await user.click(resetButton)

    expect(input).toHaveValue("")
    expect(emitted()["update:modelValue"]).toEqual([
      [
        {
          type: "courttype2",
          location: "courtlocation2",
          label: "courtlabel2",
        },
      ],
      [undefined],
    ])
  })

  it("items should be filtered", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)

    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")
  })

  it("items should stay filtered after selection", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    await user.click(dropdownItems[0])

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")

    await user.click(openDropdownContainer)

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("items should stay filtered after typing without selection", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    const closeDropdownContainer = screen.getByLabelText("Dropdown schließen")

    await user.click(closeDropdownContainer)
    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)

    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("items should be filtered if selected value exists", async () => {
    renderComponent({
      modelValue: {
        label: "courtlabel1",
        value: {
          type: "courttype1",
          location: "courtlocation1",
          label: "courtlabel1",
        },
      },
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")

    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
  })

  it("items should show message if no items matched", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel10")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
  })

  it("Dropdown item should be visible after selecting", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    await user.click(dropdownItems[0])
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    expect(input).toHaveValue("courtlabel2")

    const openDropdownContainer = screen.getByLabelText(
      "Dropdown öffnen",
    ) as HTMLElement
    await user.click(openDropdownContainer)
    // the filter is still set to "testItem2", so we expect only that one item
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(1)
  })

  it("uses endpoint to fetch all DocumentType items", async () => {
    server.use(
      http.get("api/v1/caselaw/documenttypes", () =>
        Response.json([
          {
            jurisShortcut: "AO",
            label: "AO - Anordnung",
          },
        ]),
      ),
    )

    renderComponent({
      itemService: comboboxItemService.getCaselawDocumentTypes,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("AO - Anordnung")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
  })

  it("uses endpoint to fetch all Court items", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }

    server.use(http.get("api/v1/caselaw/courts", () => Response.json([court])))

    renderComponent()

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")

    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("dropdown uses endpoint to fetch Court items based on search string", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: ComboboxItem[] = [
      { label: "BGH Karlsruhe", value: court },
    ]

    server.use(
      http.get("api/v1/caselaw/courts", () => Response.json(dropdownItems)),
    )
    renderComponent({})

    const input = screen.getByLabelText("test label")
    await user.type(input, "bgh")
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")

    // TODO checking for "b", "bg", "bgh" as the three arguments does not work though
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("court without location and with revoked string gets displayed correctly", async () => {
    const court: Court = {
      type: "ABC",
      location: "",
      label: "ABC",
      revoked: "aufgehoben seit: 1973",
    }
    server.use(http.get("api/v1/caselaw/courts", () => Response.json([court])))

    renderComponent({
      itemService: comboboxItemService.getCourts,
    })

    const openDropdownContainer = screen.getByLabelText("Dropdown öffnen")
    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("ABC")

    const additionalInfoElement = screen.getAllByLabelText(
      "additional-dropdown-info",
    )
    expect(additionalInfoElement.length).toBe(1)
    expect(additionalInfoElement[0]).toHaveTextContent("aufgehoben seit: 1973")
  })

  it("should revert to last saved state when leaving the input field via esc/tab", async () => {
    const { emitted } = renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "xxxxxx")
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    let dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")

    await user.keyboard("{escape}")
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    expect(input).toHaveValue("")

    await user.type(input, "courtlabel1")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    dropdownItems = screen.getAllByLabelText("dropdown-option")

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")

    await user.keyboard("{enter}") // save the value
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    expect(input).toHaveValue("courtlabel1")

    await user.type(input, "foo")
    await user.keyboard("{tab}")
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    // expect(input).toHaveValue("testItem1") does not work here anymore because
    // it doesn't appear in the DOM --> gets tested via e2e test instead
    // The workaround is to ensure that the model value was only updated once (on enter)
    expect(emitted()["update:modelValue"]).toHaveLength(1)
  })

  it("top search result should get chosen upon enter", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("test label")
    await user.type(input, "court")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)

    await user.keyboard("{enter}")

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    // like in previous test: workaround for not being able to read testItem1 from the DOM
    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([
      [
        {
          label: "courtlabel1",
          location: "courtlocation1",
          type: "courttype1",
        },
      ],
    ]) // value of testItem1
  })

  it("adds new entry option if manual entry flag set and no result found", async () => {
    const { emitted } = renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "foo")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("foo neu erstellen")
    await user.click(screen.getByText("foo neu erstellen"))

    expect(emitted()["update:modelValue"]).toEqual([
      [
        {
          label: "foo",
        },
      ],
    ])
  })

  it("adds new entry option if manual entry flag set and no exact match found", async () => {
    renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "court")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(4)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
    expect(dropdownItems[3]).toHaveTextContent("court neu erstellen")
  })

  it("removes new entry option when exact match found", async () => {
    renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "court")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(4)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
    expect(dropdownItems[3]).toHaveTextContent("court neu erstellen")

    await user.type(input, "courtlabel1")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.queryAllByLabelText("dropdown-option")).toHaveLength(1)
    expect(screen.queryAllByLabelText("dropdown-option")[0]).toHaveTextContent(
      "courtlabel1",
    )
    expect(screen.queryByText("testItem neu erstellen")).not.toBeInTheDocument()
  })

  it("spaces should be ignored", async () => {
    renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "testItem1 ")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("testItem1")
    expect(
      screen.queryByText("testItem1  neu erstellen"),
    ).not.toBeInTheDocument()
  })

  it("does not add new entry option if flag for manual entry is not set", async () => {
    const { emitted } = renderComponent({ manualEntry: false })

    await user.type(screen.getByLabelText("test label"), "foo")
    expect(screen.queryByText("foo neu erstellen")).not.toBeInTheDocument()

    await user.tab()

    expect(emitted()["update:modelValue"]).toBeUndefined()
  })

  it("does not render clear button if noClear flag is set", async () => {
    renderComponent({ noClear: true })

    await user.type(screen.getByLabelText("test label"), "court")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    await user.click(dropdownItems[1])

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()

    expect(
      screen.queryByLabelText("Auswahl zurücksetzen"),
    ).not.toBeInTheDocument()
  })

  it("deleting manually updates the filter", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "x")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
    input.focus()
    await user.keyboard("{Backspace}")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByLabelText("dropdown-option")).toHaveLength(3)
    expect(screen.getAllByLabelText("dropdown-option")[0]).toHaveTextContent(
      "courtlabel1",
    )
  })

  describe("keyboard navigation", () => {
    it("should select next value on arrow down", async () => {
      renderComponent()
      const input = screen.getByLabelText("test label")
      await fireEvent.focus(input)
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      const items = screen.getAllByLabelText("dropdown-option")

      input.focus()
      expect(input).toHaveFocus()

      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[0]).toHaveFocus()

      await fireEvent.keyDown(items[0], { key: "ArrowDown" })
      expect(items[1]).toHaveFocus()

      await fireEvent.keyDown(items[1], { key: "ArrowDown" })
      expect(items[2]).toHaveFocus()

      // Focus does not change on last element
      await fireEvent.keyDown(items[2], { key: "ArrowDown" })
      expect(items[2]).toHaveFocus()

      // Check that I can immediately move up even when pressing arrow-down multiple times before -> index in bounds
      await fireEvent.keyDown(items[2], { key: "ArrowUp" })
      expect(items[1]).toHaveFocus()
    })

    it("should select previous value on arrow up", async () => {
      renderComponent()
      const input = screen.getByLabelText("test label")
      await fireEvent.focus(input)
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      const items = screen.getAllByLabelText("dropdown-option")

      input.focus()
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[0]).toHaveFocus()

      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[1]).toHaveFocus()

      await fireEvent.keyDown(items[1], { key: "ArrowUp" })
      expect(items[0]).toHaveFocus()

      // Focus does not change when pressing arrow-up on first element
      await fireEvent.keyDown(items[0], { key: "ArrowUp" })
      expect(items[0]).toHaveFocus()

      // Make sure index is in bounds (not smaller than 0)
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[1]).toHaveFocus()
    })

    it("should work with createNewItem link without any items", async () => {
      server.use(http.get("api/v1/caselaw/courts", () => Response.json([])))
      renderComponent({ manualEntry: true })
      const input = screen.getByLabelText("test label")
      await fireEvent.focus(input)
      await user.type(input, "testItem")
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      const items = screen.getAllByLabelText("dropdown-option")

      expect(items[0]).not.toHaveFocus()
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[0]).toHaveFocus()
    })

    it("should work with only createNewItem link with items", async () => {
      renderComponent({ manualEntry: true })
      const input = screen.getByLabelText("test label")
      await fireEvent.focus(input)
      await user.type(input, "court")
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      const items = screen.getAllByLabelText("dropdown-option")

      // Move to createNewItem link
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      await fireEvent.keyDown(input, { key: "ArrowDown" })
      expect(items[3]).toHaveFocus()

      // Focus stays the same on last element
      await fireEvent.keyDown(items[3], { key: "ArrowDown" })
      expect(items[3]).toHaveFocus()

      // You can move up again from the focused create link element
      await fireEvent.keyDown(items[3], { key: "ArrowUp" })
      expect(items[2]).toHaveFocus()
    })
  })
})
