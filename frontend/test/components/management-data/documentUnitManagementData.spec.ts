import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { it, Mock } from "vitest"
import { nextTick, ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitManagementData from "@/components/management-data/DocumentUnitManagementData.vue"
import DocumentUnit, {
  DuplicateRelation,
  DuplicateRelationStatus,
  ManagementData,
} from "@/domain/documentUnit"
import errorMessages from "@/i18n/errors.json"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import DocumentUnitService from "@/services/documentUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

const addToastMock = vi.fn()
const ownDocumentationOffice = "BGH"
const mockedDocumentationOffices: {
  label: string
  value: { abbreviation: string }
}[] = [
  {
    label: ownDocumentationOffice,
    value: { abbreviation: ownDocumentationOffice },
  },
  { label: "BFH", value: { abbreviation: "BFH" } },
  { label: "BSG", value: { abbreviation: "BSG" } },
]
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

const server = setupServer(
  http.get("/api/v1/caselaw/documentationOffices", ({ request }) => {
    const filter = new URL(request.url).searchParams.get("q")
    const filteredItems = filter
      ? mockedDocumentationOffices.filter((item) => item.label.includes(filter))
      : mockedDocumentationOffices
    return HttpResponse.json(filteredItems)
  }),
)

describe("DocumentUnitManagementData", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.restoreAllMocks()
    setActivePinia(createTestingPinia())
    vi.mock("@/services/comboboxItemService", () => ({
      default: {
        getDocumentationOffices: () => ({
          data: ref(mockedDocumentationOffices),
          error: ref(undefined),
          loading: ref(false),
        }),
      },
    }))
    vi.mock("@/services/documentUnitService", () => ({
      default: {
        assignDocumentationOffice: vi.fn(),
      },
    }))
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 200,
      data: [],
    })
  })
  afterEach(() => {
    server.resetHandlers()
  })

  it("should show doc unit metadata", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    expect(screen.getByText(`Zuletzt bearbeitet am`)).toBeVisible()
  })

  it("should load and show doc unit history", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 200,
      data: [{ id: "1" }],
    })

    renderManagementData(managementData)
    // Wait for saving doc unit
    await nextTick()
    // Wait for loading history
    await nextTick()

    expect(screen.getByText(`Historie`)).toBeVisible()
    expect(screen.getByText(`Änderung am`)).toBeVisible()
  })

  it("should show error for doc unit history", async () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    vi.spyOn(DocumentUnitHistoryLogService, "get").mockResolvedValue({
      status: 403,
      error: { title: "Kein Zugang", description: "Nicht erlaubt" },
    })

    renderManagementData(managementData)
    // Wait for saving doc unit
    await nextTick()
    // Wait for loading history
    await nextTick()

    expect(
      screen.getByText(`Die Historie konnte nicht geladen werden.`),
    ).toBeVisible()
  })

  describe("Duplicate relations", () => {
    it("should show empty duplicate relations", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const ignoreCheckbox = screen.queryByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).not.toBeInTheDocument()

      const noDuplicateRelationsText = screen.getByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).toBeVisible()
    })

    it("should display one duplicate relation", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [duplicateRelation("dup-1")],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const duplicateRelationsText = screen.getByText("dup-1")
      expect(duplicateRelationsText).toBeVisible()

      const ignoreCheckbox = screen.getAllByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toHaveLength(1)

      const noDuplicateRelationsText = screen.queryByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).not.toBeInTheDocument()
    })

    it("should display multiple duplicate relations", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [
          duplicateRelation("dup-1"),
          duplicateRelation("dup-2"),
        ],
      }
      renderManagementData(managementData)

      const title = screen.getByText("Verwaltungsdaten")
      expect(title).toBeVisible()

      const duplicateDocNumberText1 = screen.getByText("dup-1")
      expect(duplicateDocNumberText1).toBeVisible()

      const duplicateDocNumberText2 = screen.getByText("dup-2")
      expect(duplicateDocNumberText2).toBeVisible()

      const ignoreCheckbox = screen.getAllByLabelText("Warnung ignorieren")
      expect(ignoreCheckbox).toHaveLength(2)

      const noDuplicateRelationsText = screen.queryByText(
        "Es besteht kein Dublettenverdacht.",
      )
      expect(noDuplicateRelationsText).not.toBeInTheDocument()
    })
  })

  describe("Assigning a new documentation office", () => {
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
      const { user } = renderManagementData(managementData)

      const openCombobox = screen.getByLabelText("Dropdown öffnen")
      await user.click(openCombobox)

      const options = await screen.findAllByRole("button")
      const optionLabels = options.map((o) => o.textContent)

      expect(optionLabels).toContain("BFH")
      expect(optionLabels).toContain("BSG")
      expect(optionLabels).not.toContain(ownDocumentationOffice)
    })

    it("should display error if nothing is selected", async () => {
      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [],
      }
      const { user } = renderManagementData(managementData)

      const assignButton = screen.getByRole("button", { name: "Zuweisen" })
      await user.click(assignButton)

      expect(
        screen.getByText("Wählen Sie eine Dokumentationsstelle aus"),
      ).toBeVisible()
    })

    it.skip("should show an error when assigning documentation office fails", async () => {
      ;(
        DocumentUnitService.assignDocumentationOffice as Mock
      ).mockResolvedValue({
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
      })

      const managementData: ManagementData = {
        borderNumbers: [],
        duplicateRelations: [],
      }

      const { user } = renderManagementData(managementData)

      const openCombobox = screen.getByLabelText("Dropdown öffnen")
      await user.click(openCombobox)

      const option = await screen.findByText("BFH")
      await user.click(option)

      const assignButton = screen.getByRole("button", {
        name: "Zuweisen",
      })
      await user.click(assignButton)

      const modal = await screen.findByTestId("assignDocOfficeErrorModal")
      expect(modal).toBeVisible()

      expect(
        within(modal).getByText(
          errorMessages
            .DOCUMENTATION_UNIT_DOCUMENTATION_OFFICE_COULD_NOT_BE_ASSIGNED
            .title,
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

    it.skip("should navigate to '/' and show success toast when assigning documentation office succeeded", async () => {
      ;(
        DocumentUnitService.assignDocumentationOffice as Mock
      ).mockResolvedValue({
        status: 200,
        data: {},
      })

      const { user, router } = renderManagementData({
        borderNumbers: [],
        duplicateRelations: [],
      })

      const routerPushSpy = vi.spyOn(router, "push").mockResolvedValue()

      const openCombobox = screen.getByLabelText("Dropdown öffnen")
      await user.click(openCombobox)

      const option = await screen.findByText("BFH")
      await user.click(option)

      const assignButton = screen.getByRole("button", { name: "Zuweisen" })
      await user.click(assignButton)

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

  it("should show a delete doc unit button", () => {
    const managementData: ManagementData = {
      borderNumbers: [],
      duplicateRelations: [],
    }
    renderManagementData(managementData)

    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit löschen" }),
    ).toBeVisible()
  })

  function renderManagementData(managementData: ManagementData) {
    const store = mockDocUnitStore(managementData)
    const user = userEvent.setup()

    const router = createRouter({
      history: createWebHistory(),
      routes: routes,
    })

    render(DocumentUnitManagementData, {
      global: {
        plugins: [router],
      },
    })
    return { user, store, router }
  }

  function duplicateRelation(docNumber: string): DuplicateRelation {
    return {
      documentNumber: docNumber,
      status: DuplicateRelationStatus.PENDING,
      isJdvDuplicateCheckActive: true,
    }
  }

  function mockDocUnitStore(managementData: ManagementData) {
    const mockedSessionStore = useDocumentUnitStore()
    mockedSessionStore.documentUnit = new DocumentUnit("q834", {
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
})
