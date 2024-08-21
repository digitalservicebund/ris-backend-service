import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import EditionEvaluation from "@/components/legalperiodical/EditionEvaluation.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/legalPeriodicalEditionService"
import testRoutes from "~/test-helper/routes"

async function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: testRoutes,
  })

  // Mock the route with a specific uuid before rendering
  await router.push({
    name: "caselaw-legal-periodical-editions-uuid",
    params: { uuid: "123" },
  })

  // Wait for the router to be ready
  return router.isReady().then(() => ({
    user,
    ...render(EditionEvaluation, {
      global: {
        plugins: [
          router,
          [
            createTestingPinia({
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }))
}

describe("Legal periodical edition evaluation", () => {
  beforeEach(async () => {
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

  test("renders legal periodical and edition name in title", async () => {
    await renderComponent()
    expect(screen.getByText("Periodikaauswertung | BDZ, name")).toBeVisible()
  })
})
