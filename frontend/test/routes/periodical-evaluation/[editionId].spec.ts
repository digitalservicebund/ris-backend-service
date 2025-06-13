import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnit from "@/domain/documentUnit"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import edition from "@/routes/caselaw/periodical-evaluation/[editionId]/edition.vue"
import handover from "@/routes/caselaw/periodical-evaluation/[editionId]/handover.vue"
import references from "@/routes/caselaw/periodical-evaluation/[editionId]/references.vue"
import EditionId from "@/routes/caselaw/periodical-evaluation/[editionId].vue"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()
const editionUuid = crypto.randomUUID()
const legalPeriodical: LegalPeriodical = {
  uuid: crypto.randomUUID(),
  abbreviation: "BDZ",
  title: "Bundesgesetzblatt",
  citationStyle: "2024, Heft 1",
}

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/periodical-evaluation/:editionId",
        name: "caselaw-periodical-evaluation-editionId",
        component: {},
      },
      {
        path: "/caselaw/periodical-evaluation/:editionId/references",
        name: "caselaw-periodical-evaluation-editionId-references",
        component: references,
      },
      {
        path: "/caselaw/periodical-evaluation/:editionId/edition",
        name: "caselaw-periodical-evaluation-editionId-edition",
        component: edition,
      },
      {
        path: "/caselaw/periodical-evaluation/:editionId/handover",
        name: "caselaw-periodical-evaluation-editionId-handover",
        component: handover,
      },
    ],
  })

  return {
    user,
    router,
    ...render(EditionId, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          router,
          [
            createTestingPinia({
              initialState: {
                editionStore: {
                  edition: new LegalPeriodicalEdition({
                    id: editionUuid,
                    legalPeriodical: legalPeriodical,
                    name: "name",
                    prefix: "präfix",
                    suffix: "suffix",
                    references: [],
                  }),
                },
                stubActions: false,
              },
            }),
          ],
        ],
      },
    }),
  }
}

describe("Edition Id Route", () => {
  beforeEach(() => {
    vi.spyOn(LegalPeriodicalEditionService, "save").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new LegalPeriodicalEdition({
          id: editionUuid,
          legalPeriodical: { abbreviation: "BDZ" },
          name: "name",
        }),
      }),
    )
    vi.spyOn(LegalPeriodicalEditionService, "get").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new LegalPeriodicalEdition({
          id: editionUuid,
          legalPeriodical: { abbreviation: "BDZ" },
          name: "name",
        }),
      }),
    )
    vi.spyOn(
      LegalPeriodicalEditionService,
      "getAllByLegalPeriodicalId",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          new LegalPeriodicalEdition({
            id: editionUuid,
            legalPeriodical: { abbreviation: "BDZ" },
            name: "name",
          }),
        ],
      }),
    )
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })

  describe("Conditional rendering", () => {
    test("should render edition", async () => {
      const { router } = renderComponent()

      await router.push({
        name: "caselaw-periodical-evaluation-editionId-edition",
        params: { editionId: editionUuid },
      })

      // Side Navigation
      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Info Header
      expect(
        screen.getByTestId("periodical-info-panel-title"),
      ).toBeInTheDocument()
      expect(screen.getByText("BDZ name")).toBeInTheDocument()

      // Edition route
      expect(screen.getByLabelText("Periodikum")).toHaveValue(
        "BDZ | Bundesgesetzblatt",
      )
      expect(screen.getByLabelText("Name der Ausgabe")).toHaveValue("name")
    })

    test("should render references", async () => {
      const { router } = renderComponent()

      await router.push({
        name: "caselaw-periodical-evaluation-editionId-references",
        params: { editionId: editionUuid },
      })

      // Side Navigation
      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Info Header
      expect(
        screen.getByTestId("periodical-info-panel-title"),
      ).toBeInTheDocument()
      expect(screen.getByText("BDZ name")).toBeInTheDocument()

      // References route
      expect(screen.getByTestId("references-title")).toBeInTheDocument()
    })

    test("should display extra content side panel, when docunit loaded", async () => {
      const { router } = renderComponent()
      const mockDocumentUnit = new DocumentUnit("123", {
        version: 1,
        documentNumber: "XXRE000029624",
      })
      const serviceResponse: ServiceResponse<DocumentUnit> = {
        status: 200,
        data: mockDocumentUnit,
        error: undefined,
      }

      vi.spyOn(
        documentUnitService,
        "getByDocumentNumber",
      ).mockResolvedValueOnce(serviceResponse)

      const docunitStore = useDocumentUnitStore()
      docunitStore.documentUnit = mockDocumentUnit

      await router.push({
        name: "caselaw-periodical-evaluation-editionId-references",
        params: { editionId: editionUuid },
      })

      // ExtraContentSidePanel
      expect(
        screen.getByRole("button", { name: "Seitenpanel öffnen" }),
      ).toBeInTheDocument()

      vi.resetAllMocks()
    })

    test("should render handover", async () => {
      const { router } = renderComponent()

      await router.push({
        name: "caselaw-periodical-evaluation-editionId-handover",
        params: { editionId: editionUuid },
      })

      // Side Navigation
      expect(screen.getByTestId("side-toggle-navigation")).toBeInTheDocument()

      // Info Header
      expect(
        screen.getByTestId("periodical-info-panel-title"),
      ).toBeInTheDocument()
      expect(screen.getByText("BDZ name")).toBeInTheDocument()

      // References route
      expect(screen.getByTestId("handover-title")).toBeInTheDocument()
    })
  })
})
