import { userEvent } from "@testing-library/user-event/dist/cjs/index.js"
import { render, screen, fireEvent } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest"
import DocumentationOfficeSelector from "@/components/DocumentationOfficeSelector.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import errorMessages from "@/i18n/errors.json"

const mockedDocumentationOffices = [
  {
    id: "1",
    abbreviation: "BGH",
  },
  {
    id: "2",
    abbreviation: "BFH",
  },
  {
    id: "3",
    abbreviation: "BSG",
  },
]

const server = setupServer(
  http.get("/api/v1/caselaw/documentationoffices", ({ request }) => {
    const filter = new URL(request.url).searchParams.get("q")
    const filteredItems = filter
      ? mockedDocumentationOffices.filter((item) =>
          item.abbreviation.includes(filter),
        )
      : mockedDocumentationOffices
    return HttpResponse.json(filteredItems)
  }),
)

function renderDocumentationOfficeSelector(
  modelValue: DocumentationOffice | undefined = undefined,
  excludeOfficeAbbreviations: string[] | null = null,
  hasError: boolean = false,
  styleClass: string | undefined = undefined,
) {
  return render(DocumentationOfficeSelector, {
    props: {
      modelValue,
      excludeOfficeAbbreviations,
      hasError,
      styleClass,
    },
  })
}

describe("DocumentationOfficeSelector", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.useFakeTimers({
      toFake: ["setTimeout", "clearTimeout"],
    })
  })
  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  it("should render with the correct aria-label and data-testid", () => {
    renderDocumentationOfficeSelector()
    expect(
      screen.getByLabelText("Dokumentationsstelle auswählen"),
    ).toBeVisible()
    expect(screen.getByTestId("documentation-office-combobox")).toBeVisible()
  })

  it("should display the initial documentation office if provided", () => {
    const modelValue: DocumentationOffice = {
      id: "1",
      abbreviation: "BGH",
    }
    renderDocumentationOfficeSelector(modelValue)
    expect(screen.getByDisplayValue("BGH")).toBeVisible()
  })

  it("should emit update:modelValue and update:hasError when a new value is selected", async () => {
    const { emitted } = renderDocumentationOfficeSelector()

    await fireEvent.focus(
      screen.getByLabelText("Dokumentationsstelle auswählen"),
    )
    await vi.advanceTimersByTimeAsync(300) // Debounce

    const bfhOption = screen.getByText("BFH")
    await fireEvent.click(bfhOption)

    const emittedEvents = emitted() as {
      "update:modelValue"?: (DocumentationOffice | undefined)[][]
      "update:hasError"?: boolean[][]
    }

    expect(emittedEvents["update:modelValue"]).toHaveLength(1)
    expect(emittedEvents["update:modelValue"]![0][0]).toEqual({
      id: "2",
      abbreviation: "BFH",
    })
    expect(emittedEvents["update:hasError"]).toHaveLength(1)
    expect(emittedEvents["update:hasError"]![0][0]).toBe(false)
  })

  it("should emit undefined for modelValue and false for hasError when the input is cleared", async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
    const modelValue: DocumentationOffice = {
      id: "1",
      abbreviation: "BGH",
    }
    const { emitted } = renderDocumentationOfficeSelector(modelValue)
    await user.clear(screen.getByRole("combobox"))

    const emittedEvents = emitted() as {
      "update:modelValue"?: (DocumentationOffice | undefined)[][]
      "update:hasError"?: boolean[][]
    }

    expect(emittedEvents["update:modelValue"]).toHaveLength(1)
    expect(emittedEvents["update:modelValue"]![0][0]).toBeUndefined()
    expect(emittedEvents["update:hasError"]).toHaveLength(1)
    expect(emittedEvents["update:hasError"]![0][0]).toBe(false)
  })

  it("should display all documentation offices when input is focused and no filter is applied", async () => {
    renderDocumentationOfficeSelector()

    await fireEvent.focus(
      screen.getByLabelText("Dokumentationsstelle auswählen"),
    )
    await vi.advanceTimersByTimeAsync(300) // Debounce

    expect(screen.getByText("BGH")).toBeVisible()
    expect(screen.getByText("BFH")).toBeVisible()
    expect(screen.getByText("BSG")).toBeVisible()
  })

  it("should filter documentation offices based on input", async () => {
    renderDocumentationOfficeSelector()

    const input = screen.getByLabelText("Dokumentationsstelle auswählen")
    await fireEvent.update(input, "BG")
    await vi.advanceTimersByTimeAsync(300) // Debounce

    expect(screen.getByText("BGH")).toBeVisible()
    expect(screen.queryByText("BFH")).not.toBeInTheDocument()
    expect(screen.queryByText("BSG")).not.toBeInTheDocument()

    await fireEvent.update(input, "BSG")
    await vi.advanceTimersByTimeAsync(300) // Debounce

    expect(screen.queryByText("BGH")).not.toBeInTheDocument()
    expect(screen.queryByText("BFH")).not.toBeInTheDocument()
    expect(screen.getByText("BSG")).toBeVisible()
  })

  it("should exclude specified offices from the list", async () => {
    renderDocumentationOfficeSelector(undefined, ["BGH", "BSG"])

    await fireEvent.focus(
      screen.getByLabelText("Dokumentationsstelle auswählen"),
    )
    await vi.advanceTimersByTimeAsync(300) // Debounce

    expect(screen.queryByText("BGH")).not.toBeInTheDocument()
    expect(screen.getByText("BFH")).toBeVisible()
    expect(screen.queryByText("BSG")).not.toBeInTheDocument()
  })

  it("should display an error message when hasError is true", () => {
    renderDocumentationOfficeSelector(undefined, null, true)
    expect(
      screen.getByText(errorMessages.NO_DOCUMENTATION_OFFICE_SELECTED.title),
    ).toBeVisible()
  })

  it("should apply the styleClass to the ComboboxInput component", () => {
    const customClass = "my-custom-class"
    renderDocumentationOfficeSelector(undefined, null, false, customClass)
    const comboboxInput = screen.getByTestId("documentation-office-combobox")
    expect(comboboxInput).toHaveClass(customClass)
  })

  it("should update displayed value when modelValue prop changes", async () => {
    const { rerender } = renderDocumentationOfficeSelector()

    const newOffice: DocumentationOffice = { id: "3", abbreviation: "BSG" }
    await rerender({ modelValue: newOffice })

    expect(screen.getByDisplayValue("BSG")).toBeVisible()
  })
})
