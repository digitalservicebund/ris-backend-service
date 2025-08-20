import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia, Store } from "pinia"
import { Ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionPublication from "@/components/publication/DecisionPublication.vue"
import { CoreData } from "@/domain/coreData"
import { Decision } from "@/domain/decision"
import { DuplicateRelation, ManagementData } from "@/domain/managementData"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

describe("DecisionPlausibilityCheck", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
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

    expect(screen.getByText("LDML Vorschau")).toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Veröffentlichen" }),
    ).toBeEnabled()
  })
})

async function renderComponent(
  {
    hasPlausibilityCheckPassed,
    duplicateRelations = [],
  }: {
    hasPlausibilityCheckPassed: boolean
    duplicateRelations?: DuplicateRelation[]
  } = {
    hasPlausibilityCheckPassed: true,
    duplicateRelations: [],
  },
) {
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
  mockDocUnitStore({
    coreData,
    managementData: { borderNumbers: [], duplicateRelations },
  })

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
  mockedSessionStore.documentUnit = new Decision("q834", {
    coreData,
    managementData,
  })

  return mockedSessionStore as Store<
    "docunitStore",
    {
      documentUnit: Ref<Decision>
    }
  >
}
