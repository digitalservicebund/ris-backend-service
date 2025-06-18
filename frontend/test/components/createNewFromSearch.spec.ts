import { fireEvent, render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { describe } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import CreateNewFromSearch from "@/components/CreateNewFromSearch.vue"
import { Decision } from "@/domain/decision"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnitParameters } from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import routes from "~/test-helper/routes"

const docOffice: DocumentationOffice = {
  id: "123",
  abbreviation: "BVerfG",
}

const dsDocOffice: DocumentationOffice = {
  id: "456",
  abbreviation: "DS",
}

const server = setupServer(
  http.get("/api/v1/caselaw/documentationoffices", () =>
    HttpResponse.json([docOffice, dsDocOffice]),
  ),
)

function renderComponent(options?: {
  parameters?: DocumentationUnitParameters
  isValid?: boolean
}) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return render(CreateNewFromSearch, {
    props: {
      parameters: options?.parameters ?? {},
      validateRequiredInput: () => options?.isValid ?? true,
    },
    global: { plugins: [router] },
  })
}

describe("Create new documentation unit from search", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.useFakeTimers({
      toFake: ["setTimeout", "clearTimeout", "Date"],
    })
  })
  afterEach(() => vi.useRealTimers())

  it("renders empty responsible docoffice combobox when no docOffice is given", async () => {
    renderComponent()

    const combobox = screen.getByLabelText("Dokumentationsstelle auswählen")
    expect(combobox).toBeInTheDocument()
    expect(combobox).toHaveValue("")
  })

  it("fills responsible docoffice combobox when docOffice is given in parameters", async () => {
    renderComponent({
      parameters: {
        court: { label: "Test", responsibleDocOffice: docOffice },
      },
    })

    const combobox = screen.getByLabelText("Dokumentationsstelle auswählen")
    expect(combobox).toHaveValue(docOffice.abbreviation)
  })

  it("calls service when primary button is clicked and input is valid", async () => {
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )

    renderComponent({
      parameters: {
        court: { label: "Test", responsibleDocOffice: docOffice },
      },
      isValid: true,
    })

    const button = screen.getByLabelText("Dokumentationseinheit erstellen")
    await fireEvent.click(button)

    expect(documentUnitService.createNew).toHaveBeenCalled()
  })

  it("does not call createNewFromSearch when input validation fails", async () => {
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )
    renderComponent({
      isValid: false,
    })

    const button = screen.getByLabelText("Dokumentationseinheit erstellen")
    await fireEvent.click(button)

    expect(documentUnitService.createNew).not.toHaveBeenCalled()
  })

  it("displays an error modal when the creation fails", async () => {
    const mockError = { title: "Error", description: "Something went wrong" }

    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        error: mockError,
      }),
    )

    renderComponent({
      parameters: {
        court: { label: "Test", responsibleDocOffice: docOffice },
      },
      isValid: true,
    })

    const button = screen.getByLabelText("Dokumentationseinheit erstellen")
    await fireEvent.click(button)

    const modalTitle = await screen.findByText(mockError.title)
    const modalDescription = screen.getByText(mockError.description)

    expect(modalTitle).toBeInTheDocument()
    expect(modalDescription).toBeInTheDocument()
  })

  it("opens the document unit in a new tab when the secondary button is clicked", async () => {
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )
    window.open = vi.fn()

    renderComponent({
      parameters: {
        court: { label: "Test", responsibleDocOffice: docOffice },
      },
      isValid: true,
    })

    const button = screen.getByLabelText(
      "Dokumentationseinheit erstellen und direkt bearbeiten",
    )
    await fireEvent.click(button)

    expect(window.open).toHaveBeenCalled()
  })

  it("create documentation unit with reference", async () => {
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )

    const parameters = {
      court: { label: "Test", responsibleDocOffice: docOffice },
      reference: {
        id: "f68eef21-a85c-4b90-8b2c-6ff5c16145d7",
        citation: "11111",
        referenceSupplement: "L",
        legalPeriodical: {
          uuid: "bad0cb85-3700-40fa-a112-b489aec9c124",
          title: "Arbeit & Gesundheit",
        },
      },
    } as DocumentationUnitParameters

    renderComponent({ parameters, isValid: true })

    const button = screen.getByLabelText("Dokumentationseinheit erstellen")
    await fireEvent.click(button)

    expect(documentUnitService.createNew).toHaveBeenCalledWith({
      ...parameters,
      documentationOffice: docOffice,
    })
  })

  it("create documentation unit with changed default doc office", async () => {
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )

    const parameters = {
      court: { label: "Test", responsibleDocOffice: docOffice },
    } as DocumentationUnitParameters

    await renderComponent({ parameters, isValid: true })

    await fireEvent.focus(
      await screen.findByLabelText("Dokumentationsstelle auswählen"),
    )

    // The Combobox requests are debounced
    await vi.advanceTimersByTimeAsync(300)

    // change documentation office after it was set automatically
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[1]).toHaveTextContent("DS")
    await fireEvent.click(dropdownItems[1])

    const button = screen.getByLabelText("Dokumentationseinheit erstellen")
    await fireEvent.click(button)

    expect(documentUnitService.createNew).toHaveBeenCalledWith({
      ...parameters,
      documentationOffice: dsDocOffice,
    })
  })
})
