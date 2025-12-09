import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { nextTick, Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingPlausibilityCheck from "@/components/publication/PendingProceedingPlausibilityCheck.vue"
import { CoreData } from "@/domain/coreData"
import PendingProceeding, {
  PendingProceedingShortTexts,
} from "@/domain/pendingProceeding"
import PreviousDecision from "@/domain/previousDecision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const scrollIntoViewportByIdMock = vi.fn()
vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: scrollIntoViewportByIdMock,
  }),
}))

describe("PendingProceedingPlausibilityCheck", () => {
  beforeEach(() => {
    vi.resetAllMocks()
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
  })

  describe("Required fields", () => {
    it("should warn about all required fields for an empty doc unit", async () => {
      mockDocUnitStore({})
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByText("In folgenden Rubriken fehlen Daten:"),
      ).not.toBeInTheDocument()
      expect(
        screen.getByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).toBeVisible()
      expect(screen.getByText("Aktenzeichen")).toBeVisible()
      expect(screen.getByText("Gericht")).toBeVisible()
      expect(screen.getByText("Mitteilungsdatum")).toBeVisible()
      expect(screen.getByText("Rechtsfrage")).toBeVisible()
      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("plausibilityCheckUpdated")).toEqual([[false]])
    })

    it("should warn about one required field", async () => {
      const { court, ...incompleteCoreData } = fullCoreData
      mockDocUnitStore({
        coreData: incompleteCoreData,
        shortTexts: { legalIssue: "l" },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).not.toBeInTheDocument()

      expect(
        screen.getByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).toBeVisible()

      expect(screen.getByText("Gericht")).toBeVisible()
      expect(screen.queryByText("Aktenzeichen")).not.toBeInTheDocument()
      expect(screen.queryByText("Mitteilungsdatum")).not.toBeInTheDocument()
      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("plausibilityCheckUpdated")).toEqual([[false]])
    })

    it("should show success with complete core data", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        shortTexts: { legalIssue: "l" },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()

      expect(screen.queryByText("Gericht")).not.toBeInTheDocument()
      expect(screen.queryByText("Aktenzeichen")).not.toBeInTheDocument()
      expect(screen.queryByText("Mitteilungsdatum")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("plausibilityCheckUpdated")).toEqual([[true]])
    })

    it("should redirect to categories and scroll", async () => {
      const { court, ...incompleteCoreData } = fullCoreData
      mockDocUnitStore({ coreData: incompleteCoreData })
      const { router } = await renderComponent()
      const routerSpy = vi.spyOn(router, "push").mockImplementation(vi.fn())

      await fireEvent.click(screen.getByText("Gericht"))

      expect(scrollIntoViewportByIdMock).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledWith({
        name: "caselaw-pending-proceeding-documentNumber-categories",
      })
    })

    it("should update when doc unit changes", async () => {
      const { court, ...incompleteCoreData } = fullCoreData
      const store = mockDocUnitStore({
        coreData: incompleteCoreData,
        shortTexts: { legalIssue: "l" },
      })
      const { emitted } = await renderComponent()

      // ACT: Update the core data to be complete
      store.documentUnit.coreData = fullCoreData
      await nextTick()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()

      expect(screen.queryByText("Gericht")).not.toBeInTheDocument()
      expect(screen.queryByText("Aktenzeichen")).not.toBeInTheDocument()
      expect(screen.queryByText("Mitteilungsdatum")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      // plausibility check value changes from false to true
      expect(emitted("plausibilityCheckUpdated")).toEqual([[false], [true]])
    })
  })

  describe("Missing data", () => {
    it("should warn about a category with one missing data entry", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        shortTexts: { legalIssue: "l" },
        topLevelFields: { previousDecisions: [new PreviousDecision()] },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.getByText("In folgenden Rubriken fehlen Daten:"),
      ).toBeVisible()

      expect(screen.getByText("Rubrik")).toBeVisible()
      expect(screen.getByText("Einträge mit fehlenden Daten")).toBeVisible()
      expect(screen.getByText("Vorgehende Entscheidungen")).toBeVisible()
      expect(screen.getByText("1")).toBeVisible()

      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("plausibilityCheckUpdated")).toEqual([[false]])
    })

    it("should redirect to categories and scroll", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        shortTexts: { legalIssue: "l" },
        topLevelFields: { previousDecisions: [new PreviousDecision()] },
      })
      const { router } = await renderComponent()
      const routerSpy = vi.spyOn(router, "push").mockImplementation(vi.fn())

      await fireEvent.click(screen.getByText("Vorgehende Entscheidungen"))

      expect(scrollIntoViewportByIdMock).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledWith({
        name: "caselaw-pending-proceeding-documentNumber-categories",
      })
    })

    it("should show success with complete data", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        shortTexts: { legalIssue: "l" },
        topLevelFields: {
          previousDecisions: [completePreviousDecision],
        },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()

      expect(screen.queryByText("Rubrik")).not.toBeInTheDocument()
      expect(
        screen.queryByText("Einträge mit fehlenden Daten"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByText("Vorgehende Entscheidungen"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("plausibilityCheckUpdated")).toEqual([[true]])
    })

    it("should update when doc unit changes", async () => {
      const store = mockDocUnitStore({
        coreData: fullCoreData,
        shortTexts: { legalIssue: "l" },
        topLevelFields: { previousDecisions: [new PreviousDecision()] },
      })
      const { emitted } = await renderComponent()

      // ACT: Update the norm to be complete
      store.documentUnit.previousDecisions = [completePreviousDecision]
      await nextTick()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText("In folgenden Rubriken fehlen Daten:"),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText("Vorgehende Entscheidungen"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      // plausibility check value changes from false to true
      expect(emitted("plausibilityCheckUpdated")).toEqual([[false], [true]])
    })
  })

  it("should redirect to categories via button", async () => {
    mockDocUnitStore({})
    const { router } = await renderComponent()
    const routerSpy = vi.spyOn(router, "push")
    await fireEvent.click(screen.getByText("Rubriken bearbeiten"))
    expect(routerSpy).toHaveBeenCalledOnce()
    expect(routerSpy).toHaveBeenCalledWith({
      name: "caselaw-pending-proceeding-documentNumber-categories",
    })
  })
}, 8_000)

async function renderComponent() {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  await router.push({
    name: "caselaw-pending-proceeding-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  return {
    router,
    ...render(PendingProceedingPlausibilityCheck, {
      global: { plugins: [router] },
    }),
  }
}

function mockDocUnitStore({
  shortTexts = {},
  coreData = {},
  topLevelFields = {},
}: {
  shortTexts?: PendingProceedingShortTexts
  coreData?: CoreData
  topLevelFields?: Partial<PendingProceeding>
}) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new PendingProceeding("q834", {
    ...topLevelFields,
    shortTexts,
    coreData,
  })

  return mockedSessionStore as Store<
    "docunitStore",
    {
      documentUnit: Ref<PendingProceeding>
    }
  >
}

const court = { label: "BGH" }
const fullCoreData: CoreData = {
  fileNumbers: ["IZ 1234"],
  court: court,
  decisionDate: "2024-01-01",
}

const completePreviousDecision = new PreviousDecision({
  decisionDate: "2024-01-01",
  court,
  fileNumber: "IZ 1234",
})
