import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import EditionEvaluation from "@/components/legalperiodical/EditionEvaluation.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/legalPeriodicalEditionService"
import testRoutes from "~/test-helper/routes"

function renderComponent() {
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: testRoutes,
  })
  return {
    user,
    ...render(EditionEvaluation, {
      global: { plugins: [router] },
    }),
  }
}

describe("Legal periodical edition evaluation", () => {
  beforeEach(() => {
    const legalPeriodical: LegalPeriodical = {
      abbreviation: "BDZ",
    }
    vi.spyOn(service, "get").mockImplementation(
      (): Promise<ServiceResponse<LegalPeriodicalEdition>> =>
        Promise.resolve({
          status: 200,
          data: new LegalPeriodicalEdition({
            id: crypto.randomUUID(),
            legalPeriodical: legalPeriodical,
            name: "name",
            prefix: "präfix",
            suffix: "suffix",
            references: [],
          }),
        }),
    )
  })

  test("renders correctly", async () => {
    renderComponent()
    expect(screen.getByText("Periodikaauswertung")).toBeVisible()
    // todo
  })
})
