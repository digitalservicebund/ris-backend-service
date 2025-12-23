import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { UseFetchReturn } from "@vueuse/core"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { Ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { ComboboxItem } from "@/components/input/types"
import { Court } from "@/domain/court"
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

function renderComponent<T extends object>(
  options: {
    id?: string
    modelValue?: T
    itemService?: (
      filter: Ref<string | undefined>,
    ) => UseFetchReturn<ComboboxItem<T>[]>
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

const debounceTimeout = 300

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

    const openComboboxContainer = screen.getByLabelText("Vorschläge anzeigen")
    await user.click(openComboboxContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByRole("option")).toHaveLength(3)
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

    expect(screen.getAllByRole("option")).toHaveLength(3)
  })

  it("enter should select top value", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("test label")
    await fireEvent.focus(input)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByRole("option")).toHaveLength(3)

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
    const dropdownItems = screen.getAllByRole("option")
    await user.click(dropdownItems[1])

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()

    const resetButton = screen.getByLabelText("Entfernen")
    await user.click(resetButton)

    expect(input).toHaveValue("")
    expect(emitted()["update:modelValue"]).toEqual([
      [undefined],
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

  it("user input should not be overwritten when autosave receives the currently stored value again", async () => {
    const { rerender } = renderComponent({ modelValue: { label: "court" } })
    const input = screen.getByLabelText("test label")

    await user.type(input, "{Backspace>9}new-input")

    expect(input).toHaveValue("new-input")

    // This simulates auto-save -> updates the props with same initial value
    await rerender({ modelValue: { label: "court" } })

    expect(input).toHaveValue("new-input")
  })

  it("user input should be overwritten when autosave receives a new value", async () => {
    const { rerender } = renderComponent({ modelValue: { label: "old-court" } })
    const input = screen.getByLabelText("test label")

    await user.type(input, "{Backspace>9}new-input")

    expect(input).toHaveValue("new-input")

    // This simulates auto-save -> new value from backend is received and should overwrite current input
    await rerender({ modelValue: { label: "new-court" } })

    expect(input).toHaveValue("new-court")
  })

  it("items should be filtered", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByRole("option")
    expect(dropdownItems).toHaveLength(1)

    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")
  })

  it("items should stay filtered after selection", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")

    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    await user.click(dropdownItems[0])

    const openDropdownContainer = screen.getByLabelText("Vorschläge anzeigen")

    await user.click(openDropdownContainer)

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByRole("option")).toHaveLength(1)
  })

  it("items should stay filtered after typing without selection", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")

    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    const dropdownButton = screen.getByLabelText("Vorschläge anzeigen")

    await user.click(dropdownButton)
    await user.click(dropdownButton)

    expect(screen.getAllByRole("option")).toHaveLength(1)
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

    const openDropdownContainer = screen.getByLabelText("Vorschläge anzeigen")

    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
  })

  it("items should show message if no items matched", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel10")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
  })

  it("Dropdown item should be visible after selecting", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtlabel2")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")
    expect(screen.getAllByRole("option")).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel2")

    await user.click(dropdownItems[0])
    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()
    expect(input).toHaveValue("courtlabel2")

    const openDropdownContainer = screen.getByLabelText(
      "Vorschläge anzeigen",
    ) as HTMLElement
    await user.click(openDropdownContainer)
    // the filter is still set to "testItem2", so we expect only that one item
    expect(screen.getAllByRole("option")).toHaveLength(1)
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

    const openDropdownContainer = screen.getByLabelText("Vorschläge anzeigen")
    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItems = screen.getAllByRole("option")

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

    const openDropdownContainer = screen.getByLabelText("Vorschläge anzeigen")

    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByRole("option")

    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("BGH Karlsruhe")
  })

  it("dropdown uses endpoint to fetch Court items based on search string", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }
    const dropdownItems: ComboboxItem<Court>[] = [
      { label: "BGH Karlsruhe", value: court },
    ]

    server.use(
      http.get("api/v1/caselaw/courts", () => Response.json(dropdownItems)),
    )
    renderComponent({})

    const input = screen.getByLabelText("test label")
    await user.type(input, "bgh")
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByRole("option")

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

    const openDropdownContainer = screen.getByLabelText("Vorschläge anzeigen")
    await user.click(openDropdownContainer)
    await vi.advanceTimersByTimeAsync(debounceTimeout)

    const dropdownItemElements = screen.getAllByRole("option")
    expect(dropdownItemElements).toHaveLength(1)
    expect(dropdownItemElements[0]).toHaveTextContent("ABC")

    const additionalInfoElement = screen.getAllByLabelText(
      "additional-dropdown-info",
    )
    expect(additionalInfoElement.length).toBe(1)
    expect(additionalInfoElement[0]).toHaveTextContent("aufgehoben seit: 1973")
  })

  it("top search result should get chosen upon enter", async () => {
    const { emitted } = renderComponent()
    const input = screen.getByLabelText("test label")
    await user.type(input, "court")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByRole("option")).toHaveLength(3)

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
    const dropdownItems = screen.queryAllByRole("option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("foo neu erstellen")
    await user.click(screen.getByText("foo neu erstellen"))

    expect(emitted()["update:modelValue"]).toEqual([
      [undefined],
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
    const dropdownItems = screen.queryAllByRole("option")
    expect(dropdownItems).toHaveLength(4)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
    expect(dropdownItems[3]).toHaveTextContent("court neu erstellen")
  })

  it("removes new entry option when exact match found", async () => {
    renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "court")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByRole("option")
    expect(dropdownItems).toHaveLength(4)
    expect(dropdownItems[0]).toHaveTextContent("courtlabel1")
    expect(dropdownItems[3]).toHaveTextContent("court neu erstellen")

    await user.type(input, "courtlabel1")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.queryAllByRole("option")).toHaveLength(1)
    expect(screen.queryAllByRole("option")[0]).toHaveTextContent("courtlabel1")
    expect(screen.queryByText("testItem neu erstellen")).not.toBeInTheDocument()
  })

  it("spaces should be ignored", async () => {
    renderComponent({ manualEntry: true })

    const input = screen.getByLabelText("test label")
    await user.type(input, "testItem1 ")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.queryAllByRole("option")
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

    expect(emitted()["update:modelValue"]).toEqual([[undefined]])
  })

  it("does not render clear button if noClear flag is set", async () => {
    renderComponent({ noClear: true })

    await user.type(screen.getByLabelText("test label"), "court")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")
    await user.click(dropdownItems[1])

    expect(screen.queryByLabelText("dropdown-option")).not.toBeInTheDocument()

    expect(screen.queryByLabelText("Entfernen")).not.toBeInTheDocument()
  })

  it("deleting manually updates the filter", async () => {
    renderComponent()

    const input = screen.getByLabelText("test label")
    await user.type(input, "courtx")

    await vi.advanceTimersByTimeAsync(debounceTimeout)
    const dropdownItems = screen.getAllByRole("option")
    expect(dropdownItems).toHaveLength(1)
    expect(dropdownItems[0]).toHaveTextContent("Kein passender Eintrag")
    input.focus()
    await user.keyboard("{Backspace}")
    await vi.advanceTimersByTimeAsync(debounceTimeout)
    expect(screen.getAllByRole("option")).toHaveLength(3)
    expect(screen.getAllByRole("option")[0]).toHaveTextContent("courtlabel1")
  })

  describe("keyboard navigation", () => {
    it("should select next value on arrow down", async () => {
      renderComponent()
      const input = screen.getByLabelText("test label")
      await user.click(input)
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      expect(input).toHaveFocus()

      await user.keyboard("{ArrowDown}{ArrowDown}{ArrowDown}")
      // Check that I can immediately move up even when pressing arrow-down multiple times before -> index in bounds
      await user.keyboard("{ArrowUp}")
      await user.keyboard("{Enter}")
      await vi.advanceTimersByTimeAsync(debounceTimeout)
      expect(input).toHaveValue("courtlabel2")
    })
  })
})
