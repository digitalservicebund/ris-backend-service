import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { createRouter, createWebHistory } from "vue-router"
import EditionList from "@/components/periodical-evaluation/PeriodicalEditions.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import legalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"
import routes from "~/test-helper/routes"

const server = setupServer(
  http.get("/api/v1/caselaw/legalperiodicals", () => {
    const legalPeriodical: LegalPeriodical = {
      abbreviation: "BDZ",
    }
    return HttpResponse.json([legalPeriodical])
  }),
)

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(EditionList, {
      global: {
        plugins: [
          router,
          [
            createTestingPinia({
              initialState: {
                editionStore: undefined,
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("Legal periodical edition list", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    const legalPeriodical: LegalPeriodical = {
      abbreviation: "BDZ",
    }
    const dropdownLegalPeriodicalEditions: LegalPeriodicalEdition[] = [
      new LegalPeriodicalEdition({
        id: "1",
        legalPeriodical: legalPeriodical,
        name: "2024, Heft 1",
        references: [],
        prefix: "2024",
        suffix: "Heft 1",
      }),
    ]
    vi.spyOn(
      legalPeriodicalEditionService,
      "getAllByLegalPeriodicalId",
    ).mockImplementation(() =>
      Promise.resolve({ status: 200, data: dropdownLegalPeriodicalEditions }),
    )
  })

  test("renders correctly", async () => {
    renderComponent()

    expect(screen.getByText("Periodika")).toBeVisible()
  })

  test.skip("selecting legal periodical from combobox updates filter", async () => {
    const { user } = renderComponent()
    const periodicalField = screen.getByLabelText("Periodikum")

    await user.type(periodicalField, "BDZ")
    const dropdownItems = screen.getAllByRole("option")
    expect(dropdownItems[0]).toHaveTextContent("BDZ")
    await user.click(dropdownItems[0])
    expect(periodicalField).toHaveValue("BDZ")
    expect(
      screen.getByText("Ausgabe 2024, Heft 1 (0 Fundstellen)"),
    ).toBeVisible()
    expect(screen.getByText("bearbeiten")).toBeVisible()
  })
})
