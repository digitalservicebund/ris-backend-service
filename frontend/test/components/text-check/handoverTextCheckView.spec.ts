import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import HandoverTextCheckView from "@/components/text-check/HandoverTextCheckView.vue"
import errorMessages from "@/i18n/errors.json"
import featureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { TextCheckAllResponse } from "@/types/textCheck"
import routes from "~/test-helper/routes"
import { suggestions } from "~/test-helper/text-check-service-mock"

function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(HandoverTextCheckView, {
      global: {
        plugins: [router, [createTestingPinia({})]],
      },
      props: {
        documentNumber: "TEST000011225",
        documentId: crypto.randomUUID(),
      },
    }),
  }
}

describe("text check handover", () => {
  beforeEach(async () => {
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })

    vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
      status: 200,
      data: {
        suggestions: suggestions,
        totalTextCheckErrors: 2,
        categoryTypes: ["tenor", "reasons"],
      },
    } as ServiceResponse<TextCheckAllResponse>)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })
  it("failed response display an error", async () => {
    vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
      status: 400,
      error: {
        title: errorMessages.TEXT_CHECK_FAILED.title,
      },
      data: undefined,
    } as ServiceResponse<TextCheckAllResponse>)

    renderComponent()

    expect(
      await screen.findByText(errorMessages.TEXT_CHECK_FAILED.title),
    ).toBeInTheDocument()
  })

  it("successful response list text categories and error count", async () => {
    renderComponent()

    expect(await screen.findByText("Tenor, Gründe")).toBeInTheDocument()
    expect(screen.queryByTestId("total-text-check-errors")).toHaveTextContent(
      "2",
    )
    expect(
      screen.queryByText(errorMessages.TEXT_CHECK_FAILED.title),
    ).not.toBeInTheDocument()
  })
})
