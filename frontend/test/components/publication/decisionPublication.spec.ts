import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPublication from "@/components/publication/DecisionPublication.vue"
import { CoreData } from "@/domain/coreData"
import { Decision } from "@/domain/decision"
import {
  DuplicateRelation,
  DuplicateRelationStatus,
  ManagementData,
} from "@/domain/managementData"
import borderNumberService from "@/services/borderNumberService"
import documentUnitService from "@/services/documentUnitService"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const previewMock = vi.spyOn(publishDocumentationUnitService, "getPreview")

vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: vi.fn(),
  }),
}))

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
    previewMock.mockResolvedValue({
      status: 200,
      data: {
        ldml: "ldml",
        success: true,
      },
    })

    vi.spyOn(documentUnitService, "update").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          documentationUnitVersion: 2,
          patch: [],
          errorPaths: [],
        },
      }),
    )

    vi.spyOn(documentUnitService, "getByDocumentNumber").mockImplementation(
      () => {
        if (!localDocUnit) {
          throw new Error(
            "localDocUnit must be defined before calling getByDocumentNumber",
          )
        }

        return Promise.resolve({
          status: 200,
          data: new Decision("q834", JSON.parse(JSON.stringify(localDocUnit))),
        })
      },
    )

    vi.spyOn(
      publishDocumentationUnitService,
      "publishDocument",
    ).mockResolvedValue({ status: 200, data: undefined })
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it("should not show LDML preview if plausibility check fails", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: false })

    expect(
      screen.getByText(
        "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
      ),
    ).toBeInTheDocument()

    expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
  })

  it("should not allow publishing if plausibility check fails", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: false })

    expect(
      screen.getByText(
        "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
      ),
    ).toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    ).toBeDisabled()
  })

  it("should render all child components when plausibility check is true", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: true })

    expect(
      screen.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
    ).toBeInTheDocument()

    expect(screen.getByText("Randnummernprüfung")).toBeInTheDocument()

    expect(screen.getByText("Dublettenprüfung")).toBeInTheDocument()

    expect(screen.getByText("Rechtschreibprüfung")).toBeInTheDocument()

    expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    ).toBeEnabled()
  })

  it("should not pass on publication warnings for ignored duplicate warnings", async () => {
    await renderComponent({
      hasPlausibilityCheckPassed: true,
      duplicateRelations: [
        {
          documentNumber: "KORE",
          status: DuplicateRelationStatus.IGNORED,
          isJdvDuplicateCheckActive: true,
        },
      ],
    })

    await fireEvent.click(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    )

    expect(
      screen.queryByRole("button", { name: "Trotzdem übergeben" }),
    ).not.toBeInTheDocument()
  })

  it("should pass on publication warnings for pending duplicate warnings", async () => {
    await renderComponent({
      hasPlausibilityCheckPassed: true,
      duplicateRelations: [
        {
          documentNumber: "KORE",
          status: DuplicateRelationStatus.PENDING,
          isJdvDuplicateCheckActive: true,
        },
      ],
    })

    await fireEvent.click(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    )

    expect(
      screen.getByText(
        "Es besteht Dublettenverdacht. Wollen Sie das Dokument dennoch übergeben?",
      ),
    ).toBeInTheDocument()
  })

  it("should pass on publication warnings for invalid border numbers", async () => {
    vi.spyOn(
      publishDocumentationUnitService,
      "publishDocument",
    ).mockResolvedValue({ status: 200, data: undefined })
    vi.spyOn(borderNumberService, "validateBorderNumberLinks").mockReturnValue({
      isValid: false,
      invalidCategories: [],
    })
    await renderComponent({ hasPlausibilityCheckPassed: true })

    await fireEvent.click(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    )

    expect(
      screen.getByText(
        "Die Randnummern sind nicht korrekt. Wollen Sie das Dokument dennoch übergeben?",
      ),
    ).toBeInTheDocument()
  })

  it("should reload the doc unit when border numbers are recalculated", async () => {
    vi.spyOn(borderNumberService, "validateBorderNumbers").mockReturnValue({
      isValid: false,
      hasError: false,
      expectedBorderNumber: 2,
      invalidCategory: "tenor",
      firstInvalidBorderNumber: "",
    })
    const { store } = await renderComponent({
      hasPlausibilityCheckPassed: true,
    })
    const updateDocUnitSpy = vi.spyOn(store, "updateDocumentUnit")
    updateDocUnitSpy.mockClear()

    await fireEvent.click(
      screen.getByRole("button", { name: "Randnummern neu berechnen" }),
    )

    expect(updateDocUnitSpy).toHaveBeenCalledOnce()
  })

  it("should reload the preview when border numbers are recalculated", async () => {
    vi.spyOn(borderNumberService, "validateBorderNumbers").mockReturnValue({
      isValid: false,
      hasError: false,
      expectedBorderNumber: 2,
      invalidCategory: "tenor",
      firstInvalidBorderNumber: "",
    })
    await renderComponent({ hasPlausibilityCheckPassed: true })

    await fireEvent.click(
      screen.getByRole("button", { name: "Randnummern neu berechnen" }),
    )

    expect(previewMock).toHaveBeenCalledTimes(2)
  })

  describe("ldml preview", () => {
    it("should display ldml preview whit plausible data", async () => {
      vi.spyOn(
        borderNumberService,
        "validateBorderNumberLinks",
      ).mockReturnValue({
        isValid: true,
      })

      await renderComponent({
        hasPlausibilityCheckPassed: true,
      })

      expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()

      // Expand preview
      await fireEvent.click(screen.getByLabelText("Aufklappen"))
      expect(await screen.findByTestId("code-snippet")).toBeInTheDocument()
    })

    it("should not display ldml preview with implausible data", async () => {
      await renderComponent({
        hasPlausibilityCheckPassed: false,
      })

      expect(screen.queryByText("LDML Vorschau")).not.toBeInTheDocument()
      expect(previewMock).not.toHaveBeenCalled()
    })

    it("should show error when ldml preview cannot be loaded", async () => {
      const description =
        "Die LDML-Vorschau konnte nicht geladen werden: Aktuelle Fehlermeldung."
      previewMock.mockResolvedValue({
        status: 422,
        error: {
          title: "Fehler beim Laden der LDML-Vorschau",
          description: description,
        },
      })
      await renderComponent({
        hasPlausibilityCheckPassed: true,
      })

      expect(await screen.findByText(description)).toBeInTheDocument()
    })

    it("should not allow publishing when ldml preview cannot be loaded", async () => {
      previewMock.mockResolvedValue({
        status: 422,
        error: { title: "Error", description: "Error" },
      })
      await renderComponent({
        hasPlausibilityCheckPassed: true,
      })

      expect(
        await screen.findByRole("button", { name: "Veröffentlichen" }),
      ).toBeDisabled()
    })
  })
})

let localDocUnit: Decision | undefined = undefined

async function renderComponent({
  hasPlausibilityCheckPassed,
  duplicateRelations = [],
}: {
  hasPlausibilityCheckPassed: boolean
  duplicateRelations?: DuplicateRelation[]
}) {
  let coreData: CoreData = {}
  if (hasPlausibilityCheckPassed) {
    coreData = {
      fileNumbers: ["IZ 1234"],
      court: { label: "BGH" },
      decisionDate: "2024-01-01",
      documentType: { label: "Urteil", jurisShortcut: "U" },
      legalEffect: "unbestimmt",
    }
  }
  const managementData = duplicateRelations
    ? { borderNumbers: [], duplicateRelations }
    : undefined
  const store = mockDocUnitStore({ coreData, managementData })

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  return {
    store,
    router,
    ...render(DecisionPublication, {
      global: {
        plugins: [router],
      },
    }),
  }
}

function mockDocUnitStore({
  coreData,
  managementData,
}: {
  coreData?: CoreData
  managementData?: ManagementData
}) {
  const mockedSessionStore = useDocumentUnitStore()
  localDocUnit = new Decision("q834", {
    coreData,
    managementData,
  })
  mockedSessionStore.documentUnit = localDocUnit

  return mockedSessionStore
}
