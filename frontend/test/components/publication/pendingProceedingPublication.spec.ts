import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingPublication from "@/components/publication/PendingProceedingPublication.vue"
import { CoreData } from "@/domain/coreData"
import PendingProceeding from "@/domain/pendingProceeding"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const previewMock = vi.spyOn(publishDocumentationUnitService, "getPreview")

describe("PendingProceedingPlausibilityCheck", () => {
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
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("should render all child components when plausibility check is true", async () => {
    await renderComponent({ hasPlausibilityCheckPassed: true })

    expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    ).toBeEnabled()
  })

  describe("ldml preview", () => {
    it("should display ldml preview whit plausible data", async () => {
      await renderComponent({
        hasPlausibilityCheckPassed: true,
      })

      expect(await screen.findByText("LDML Vorschau")).toBeInTheDocument()

      // Expand preview
      await fireEvent.click(screen.getByLabelText("Aufklappen"))
      expect(await screen.findByTestId("code-snippet")).toBeInTheDocument()
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

async function renderComponent({
  hasPlausibilityCheckPassed,
}: {
  hasPlausibilityCheckPassed: boolean
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
  const store = mockDocUnitStore({ coreData })

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  await router.push({
    name: "caselaw-pending-proceeding-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  return {
    store,
    router,
    ...render(PendingProceedingPublication, {
      global: {
        plugins: [router],
      },
    }),
  }
}

function mockDocUnitStore({ coreData }: { coreData?: CoreData }) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new PendingProceeding("q834", {
    coreData,
  })

  return mockedSessionStore
}
