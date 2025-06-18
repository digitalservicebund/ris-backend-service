import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen, within } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { describe, it } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitManagementData from "@/components/management-data/DocumentUnitManagementData.vue"
import { Decision } from "@/domain/decision"
import { ManagementData } from "@/domain/managementData"
import errorMessages from "@/i18n/errors.json"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import documentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

const ownDocumentationOffice = "BGH"

const mockedDocumentationOffices = [
  {
    id: "123",
    abbreviation: ownDocumentationOffice,
  },
  {
    id: "456",
    abbreviation: "BFH",
  },
  {
    id: "789",
    abbreviation: "BSG",
  },
]

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))
vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
  status: 200,
  data: [],
})

const server = setupServer(
  http.get("/api/v1/caselaw/documentationoffices", () =>
    HttpResponse.json(mockedDocumentationOffices),
  ),
  http.get("/api/v1/caselaw/documentunits/:id/historylogs", () =>
    HttpResponse.json([]),
  ),
)

function renderManagementData(managementData: ManagementData) {
  const store = mockDocUnitStore(managementData)

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  render(DocumentUnitManagementData, {
    global: {
      plugins: [router],
    },
  })
  return { store, router }
}

describe("Assigning a new documentation office", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.restoreAllMocks()
    setActivePinia(createTestingPinia())
    vi.useFakeTimers({
      toFake: ["setTimeout", "clearTimeout", "Date"],
    })
  })
  afterEach(() => vi.useRealTimers())

  it("should display assign option for new documentation office", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    expect(screen.getByText("Verwaltungsdaten")).toBeVisible()
    expect(
      screen.getByTestId("assign-documentation-office-title"),
    ).toBeVisible()
    expect(screen.getByTestId("documentation-office-combobox")).toBeVisible()
    expect(screen.getByRole("button", { name: "Zuweisen" })).toBeVisible()
  })

  it("should display all documentation offices except own in dropdown", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    await fireEvent.focus(
      await screen.findByLabelText("Dokumentationsstelle auswählen"),
    )

    // The Combobox requests are debounced
    await vi.advanceTimersByTimeAsync(300)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    const optionLabels = dropdownItems.map((o) => o.textContent)

    expect(optionLabels).toContain("BFH")
    expect(optionLabels).toContain("BSG")
    expect(optionLabels).not.toContain(ownDocumentationOffice)
  })

  it("should display error if nothing is selected", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    const button = screen.getByLabelText("Zuweisen")
    await fireEvent.click(button)

    expect(
      screen.getByText("Wählen Sie eine Dokumentationsstelle aus"),
    ).toBeVisible()
  })

  it("should show an error when assigning documentation office fails", async () => {
    vi.spyOn(
      documentUnitService,
      "assignDocumentationOffice",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error: {
          title:
            errorMessages
              .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
              .title,
          description:
            errorMessages
              .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
              .description,
        },
      }),
    )

    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }

    renderManagementData(managementData)

    await fireEvent.focus(
      await screen.findByLabelText("Dokumentationsstelle auswählen"),
    )

    // The Combobox requests are debounced
    await vi.advanceTimersByTimeAsync(300)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    await fireEvent.click(dropdownItems[0])

    const button = screen.getByLabelText("Zuweisen")
    await fireEvent.click(button)

    const modal = await screen.findByTestId("assignDocOfficeErrorModal")
    expect(modal).toBeVisible()

    expect(
      within(modal).getByText(
        errorMessages
          .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED.title,
      ),
    ).toBeVisible()

    expect(
      within(modal).getByText(
        errorMessages
          .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
          .description,
      ),
    ).toBeVisible()
  })

  it("should navigate to '/' and show success toast when assigning documentation office succeeded", async () => {
    vi.spyOn(
      documentUnitService,
      "assignDocumentationOffice",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {},
      }),
    )

    const { router } = renderManagementData({
      borderNumbers: [],
      duplicateRelations: [],
    })

    const routerPushSpy = vi.spyOn(router, "push").mockResolvedValue()

    await fireEvent.focus(
      await screen.findByLabelText("Dokumentationsstelle auswählen"),
    )

    // The Combobox requests are debounced
    await vi.advanceTimersByTimeAsync(300)

    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    await fireEvent.click(dropdownItems[0])

    const button = screen.getByLabelText("Zuweisen")
    await fireEvent.click(button)

    expect(routerPushSpy).toHaveBeenCalledWith({ path: "/" })
    expect(addToastMock).toHaveBeenCalledExactlyOnceWith({
      detail:
        "Die Dokumentationseinheit DS123 ist jetzt in der Zuständigkeit der Dokumentationsstelle BFH.",
      life: 5000,
      severity: "success",
      summary: "Zuweisen erfolgreich",
    })
  })
})
function mockDocUnitStore(managementData: ManagementData) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new Decision("q834", {
    documentNumber: "DS123",
    coreData: {
      documentationOffice: {
        id: "documentationOfficeId",
        abbreviation: ownDocumentationOffice,
      },
    },
    managementData,
  })
  vi.spyOn(mockedSessionStore, "updateDocumentUnit").mockResolvedValue({
    status: 200,
    data: { documentationUnitVersion: 0, patch: [], errorPaths: [] },
  })

  return mockedSessionStore
}
