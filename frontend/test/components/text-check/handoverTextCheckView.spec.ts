import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { createRouter, createWebHistory } from "vue-router"
import HandoverTextCheckView from "@/components/text-check/HandoverTextCheckView.vue"
import errorMessages from "@/i18n/errors.json"
import featureToggleService from "@/services/featureToggleService"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { TextCheckAllResponse } from "@/types/textCheck"
import routes from "~/test-helper/routes"
import { suggestions } from "~/test-helper/text-check-service-mock"

async function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  await flushPromises()
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
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })
  it("successful response list text categories and error count", async () => {
    const expectedError = 494
    const enabledTextCheckCategories: Record<string, string> = {
      tenor: "Tenor",
      reasons: "Gründe",
      caseFacts: "Tatbestand",
      decisionReasons: "Entscheidungsgründe",
      headnote: "Orientierungssatz",
      otherHeadnote: "Sonstiger Orientierungssatz",
      guidingPrinciple: "Leitsatz",
      headline: "Titelzeile",
      otherLongText: "Sonstiger Langtext",
      dissentingOpinion: "Abweichende Meinung",
      outline: "Gliederung",
    }

    vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
      status: 200,
      data: {
        suggestions: suggestions,
        totalTextCheckErrors: expectedError,
        categoryTypes: Object.keys(enabledTextCheckCategories),
      },
    } as ServiceResponse<TextCheckAllResponse>)

    await renderComponent()

    Object.entries(enabledTextCheckCategories).forEach(([key, value]) => {
      const element = screen.getByTestId(`text-check-handover-link-${key}`)
      expect(element).toHaveTextContent(value)

      expect(element).toHaveAttribute(
        "href",
        `/caselaw/documentUnit/TEST000011225/categories#${key}`,
      )
    })

    expect(screen.queryByTestId("total-text-check-errors")).toHaveTextContent(
      expectedError.toString(),
    )
    expect(
      screen.queryByText(errorMessages.TEXT_CHECK_FAILED.title),
    ).not.toBeInTheDocument()
  })
})
