import { createTestingPinia } from "@pinia/testing"
import { render, fireEvent, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { nextTick, Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPlausibilityCheck from "@/components/publication/DecisionPlausibilityCheck.vue"
import ActiveCitation from "@/domain/activeCitation"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { CoreData } from "@/domain/coreData"
import { Decision, LongTexts } from "@/domain/decision"
import EnsuingDecision from "@/domain/ensuingDecision"
import LegalForce from "@/domain/legalForce"
import NormReference from "@/domain/normReference"
import PreviousDecision from "@/domain/previousDecision"
import SingleNorm from "@/domain/singleNorm"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const scrollIntoViewportByIdMock = vi.fn()
vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: scrollIntoViewportByIdMock,
  }),
}))

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
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
      expect(screen.getByText("Entscheidungsdatum")).toBeVisible()
      expect(screen.getByText("Rechtskraft")).toBeVisible()
      expect(screen.getByText("Dokumenttyp")).toBeVisible()
      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should warn about one required field", async () => {
      const { court, ...incompleteCoreData } = fullCoreData
      mockDocUnitStore({ coreData: incompleteCoreData })
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
      expect(screen.queryByText("Entscheidungsdatum")).not.toBeInTheDocument()
      expect(screen.queryByText("Rechtskraft")).not.toBeInTheDocument()
      expect(screen.queryByText("Dokumenttyp")).not.toBeInTheDocument()
      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should show success with complete core data", async () => {
      mockDocUnitStore({ coreData: fullCoreData })
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
      expect(screen.queryByText("Entscheidungsdatum")).not.toBeInTheDocument()
      expect(screen.queryByText("Rechtskraft")).not.toBeInTheDocument()
      expect(screen.queryByText("Dokumenttyp")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("updatePlausibilityCheck")).toEqual([[true]])
    })

    it("should update when doc unit changes", async () => {
      const { court, ...incompleteCoreData } = fullCoreData
      const store = mockDocUnitStore({ coreData: incompleteCoreData })
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
      expect(screen.queryByText("Entscheidungsdatum")).not.toBeInTheDocument()
      expect(screen.queryByText("Rechtskraft")).not.toBeInTheDocument()
      expect(screen.queryByText("Dokumenttyp")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      // plausibility check value changes from false to true
      expect(emitted("updatePlausibilityCheck")).toEqual([[false], [true]])
    })
  })

  describe("Missing data", () => {
    it("should warn about a category with one missing data entry", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        topLevelFields: { ensuingDecisions: [new EnsuingDecision()] },
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
      expect(screen.getByText("Nachgehende Entscheidungen")).toBeVisible()
      expect(screen.getByText("1")).toBeVisible()

      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should redirect to categories and scroll", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        topLevelFields: { ensuingDecisions: [new EnsuingDecision()] },
      })
      const { router } = await renderComponent()
      const routerSpy = vi.spyOn(router, "push").mockImplementation(vi.fn())

      await fireEvent.click(screen.getByText("Nachgehende Entscheidungen"))

      expect(scrollIntoViewportByIdMock).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledWith({
        name: "caselaw-documentUnit-documentNumber-categories",
      })
    })

    it("should warn about a category with multiple missing data entries", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        contentRelatedIndexing: {
          activeCitations: [
            new ActiveCitation(),
            completeCitation,
            new ActiveCitation(),
          ],
        },
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
      expect(screen.getByText("Aktivzitierung")).toBeVisible()
      expect(screen.getByText("2")).toBeVisible()

      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should show all incomplete data warnings", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        contentRelatedIndexing: {
          activeCitations: [new ActiveCitation()],
          norms: [incompleteNorm],
        },
        topLevelFields: {
          ensuingDecisions: [new EnsuingDecision()],
          previousDecisions: [new PreviousDecision()],
        },
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

      expect(screen.getAllByText("Rubrik")).toHaveLength(4)
      expect(screen.getAllByText("Einträge mit fehlenden Daten")).toHaveLength(
        4,
      )
      expect(screen.getByText("Vorgehende Entscheidungen")).toBeInTheDocument()
      expect(screen.getByText("Nachgehende Entscheidungen")).toBeInTheDocument()
      expect(screen.getByText("Aktivzitierung")).toBeInTheDocument()
      expect(screen.getByText("Normen")).toBeInTheDocument()
      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeInTheDocument()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should show success with complete data", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        contentRelatedIndexing: {
          activeCitations: [completeCitation],
          norms: [completeNorm],
        },
        topLevelFields: {
          ensuingDecisions: [completeEnsuingDecision],
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
        screen.queryByText("Nachgehende Entscheidungen"),
      ).not.toBeInTheDocument()
      expect(screen.queryByText("Aktivzitierung")).not.toBeInTheDocument()
      expect(screen.queryByText("Normen")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("updatePlausibilityCheck")).toEqual([[true]])
    })

    it("should update when doc unit changes", async () => {
      const store = mockDocUnitStore({
        coreData: fullCoreData,
        contentRelatedIndexing: { norms: [incompleteNorm] },
      })
      const { emitted } = await renderComponent()

      // ACT: Update the norm to be complete
      store.documentUnit.contentRelatedIndexing.norms = [completeNorm]
      await nextTick()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText("In folgenden Rubriken fehlen Daten:"),
      ).not.toBeInTheDocument()

      expect(screen.queryByText("Normen")).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      // plausibility check value changes from false to true
      expect(emitted("updatePlausibilityCheck")).toEqual([[false], [true]])
    })
  })

  describe("Long texts", () => {
    it("should warn about Gründe with Entscheidungsgründe", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        longTexts: {
          reasons: "Gründe",
          decisionReasons: "Entscheidungsgründe",
        },
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
        screen.getByText(
          /Die Rubriken "Gründe" und "Entscheidungsgründe" sind befüllt. Es darf nur eine der beiden Rubriken befüllt sein./,
        ),
      ).toBeVisible()

      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should warn about Gründe with Tatbestand", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        longTexts: {
          reasons: "Gründe",
          caseFacts: "Tatbestand",
        },
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
        screen.getByText(/Die Rubriken "Gründe" und "Tatbestand" sind befüllt/),
      ).toBeVisible()

      expect(
        screen.getByRole("button", { name: "Rubriken bearbeiten" }),
      ).toBeVisible()
      expect(emitted("updatePlausibilityCheck")).toEqual([[false]])
    })

    it("should show success with Entscheidungsgründe+Tatbestand", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        longTexts: {
          decisionReasons: "Entscheidungsgründe",
          caseFacts: "Tatbestand",
        },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          /Die Rubriken "Gründe" und "Tatbestand" sind befüllt/,
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("updatePlausibilityCheck")).toEqual([[true]])
    })

    it("should show success with Gründe", async () => {
      mockDocUnitStore({
        coreData: fullCoreData,
        longTexts: { reasons: "Gründe" },
      })
      const { emitted } = await renderComponent()
      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          /Die Rubriken "Gründe" und "Tatbestand" sind befüllt/,
        ),
      ).not.toBeInTheDocument()

      expect(screen.queryByText("Rubrik")).not.toBeInTheDocument()
      expect(
        screen.queryByText("Einträge mit fehlenden Daten"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      expect(emitted("updatePlausibilityCheck")).toEqual([[true]])
    })

    it("should update when long text is removed", async () => {
      const store = mockDocUnitStore({
        coreData: fullCoreData,
        longTexts: { reasons: "Gründe", caseFacts: "Tatbestand" },
      })
      const { emitted } = await renderComponent()

      // ACT: Update the norm to be complete
      store.documentUnit.longTexts.caseFacts = undefined
      await nextTick()

      expect(
        screen.queryByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
      ).toBeVisible()

      expect(
        screen.queryByText(
          /Die Rubriken "Gründe" und "Tatbestand" sind befüllt/,
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByRole("button", { name: "Rubriken bearbeiten" }),
      ).not.toBeInTheDocument()
      // plausibility check value changes from false to true
      expect(emitted("updatePlausibilityCheck")).toEqual([[false], [true]])
    })
  })

  it("should redirect to categories via button", async () => {
    mockDocUnitStore({})
    const { router } = await renderComponent()
    const routerSpy = vi.spyOn(router, "push")
    await fireEvent.click(screen.getByText("Rubriken bearbeiten"))
    expect(routerSpy).toHaveBeenCalledOnce()
    expect(routerSpy).toHaveBeenCalledWith({
      name: "caselaw-documentUnit-documentNumber-categories",
    })
  })
}, 8_000)

async function renderComponent() {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  await router.push({
    name: "caselaw-documentUnit-documentNumber-publication",
    params: { documentNumber: "KORE123412345" },
  })
  return {
    router,
    ...render(DecisionPlausibilityCheck, { global: { plugins: [router] } }),
  }
}

function mockDocUnitStore({
  contentRelatedIndexing = {},
  longTexts = {},
  coreData = {},
  topLevelFields = {},
}: {
  contentRelatedIndexing?: ContentRelatedIndexing
  longTexts?: LongTexts
  coreData?: CoreData
  topLevelFields?: Partial<Decision>
}) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new Decision("q834", {
    ...topLevelFields,
    contentRelatedIndexing,
    longTexts,
    coreData,
  })

  return mockedSessionStore as Store<
    "docunitStore",
    {
      documentUnit: Ref<Decision>
    }
  >
}

const documentType = { label: "Urteil", jurisShortcut: "U" }
const court = { label: "BGH" }
const fullCoreData: CoreData = {
  fileNumbers: ["IZ 1234"],
  court: court,
  decisionDate: "2024-01-01",
  documentType,
  legalEffect: "unbestimmt",
}

const completeCitation = new ActiveCitation({
  court,
  citationType: { label: "" },
  fileNumber: "IZ 1234",
  decisionDate: "2024-01-01",
})
const completeEnsuingDecision = new EnsuingDecision({
  decisionDate: "2024-01-01",
  court,
  fileNumber: "IZ 1234",
})
const completePreviousDecision = new PreviousDecision({
  decisionDate: "2024-01-01",
  court,
  fileNumber: "IZ 1234",
})

const incompleteNorm = new NormReference({
  singleNorms: [new SingleNorm({ legalForce: new LegalForce() })],
})
const completeNorm = new NormReference({
  singleNorms: [
    new SingleNorm({
      legalForce: new LegalForce({
        type: { abbreviation: "" },
        region: { longText: "" },
      }),
    }),
  ],
})
