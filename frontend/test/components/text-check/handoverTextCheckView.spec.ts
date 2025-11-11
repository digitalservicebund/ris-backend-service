import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { createRouter, createWebHistory } from "vue-router"
import HandoverTextCheckView from "@/components/text-check/HandoverTextCheckView.vue"
import { Kind } from "@/domain/documentationUnitKind"
import errorMessages from "@/i18n/errors.json"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { TextCheckAllResponse } from "@/types/textCheck"
import routes from "~/test-helper/routes"
import { suggestions } from "~/test-helper/text-check-service-mock"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

async function renderComponent(kind: Kind) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return {
    user,
    ...render(HandoverTextCheckView, {
      global: {
        plugins: [router, createTestingPinia({})],
      },
      props: {
        documentNumber: "TEST000011225",
        documentId: crypto.randomUUID(),
        kind,
      },
    }),
    router,
  }
}

describe("text check handover", () => {
  beforeEach(async () => {
    useFeatureToggleServiceMock()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe("for decisions", () => {
    it("displays links to categories when text mistakes are found", async () => {
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

      const { user, router } = await renderComponent(Kind.DECISION)

      await flushPromises()

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

      const button = screen.getByRole("button", { name: "Rubriken bearbeiten" })
      expect(button).toBeVisible()

      const routerSpy = vi.spyOn(router, "push")

      await user.click(button)

      expect(routerSpy).toHaveBeenCalledOnce()
    })

    it("displays success message when no text mistakes are found", async () => {
      const expectedError = 0

      vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
        status: 200,
        data: {
          suggestions: [],
          totalTextCheckErrors: expectedError,
          categoryTypes: [],
        },
      } as ServiceResponse<TextCheckAllResponse>)

      await renderComponent(Kind.DECISION)

      await flushPromises()

      expect(
        screen.queryByText(errorMessages.TEXT_CHECK_FAILED.title),
      ).not.toBeInTheDocument()

      expect(
        screen.getByText("Es wurden keine Rechtschreibfehler identifiziert."),
      ).toBeInTheDocument()
    })
  })

  describe("for pending proceedings", () => {
    beforeEach(async () => {
      useFeatureToggleServiceMock()
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })
    it("displays links to categories when text mistakes are found", async () => {
      const expectedError = 494
      const enabledTextCheckCategories: Record<string, string> = {
        headline: "Titelzeile",
        legalIssue: "Rechtsfrage",
        resolutionNote: "Erledigungsvermerk",
      }

      vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
        status: 200,
        data: {
          suggestions: suggestions,
          totalTextCheckErrors: expectedError,
          categoryTypes: Object.keys(enabledTextCheckCategories),
        },
      } as ServiceResponse<TextCheckAllResponse>)

      const { user, router } = await renderComponent(Kind.PENDING_PROCEEDING)

      await flushPromises()

      Object.entries(enabledTextCheckCategories).forEach(([key, value]) => {
        const element = screen.getByTestId(`text-check-handover-link-${key}`)
        expect(element).toHaveTextContent(value)

        expect(element).toHaveAttribute(
          "href",
          `/caselaw/pendingProceeding/TEST000011225/categories#${key}`,
        )
      })

      expect(screen.queryByTestId("total-text-check-errors")).toHaveTextContent(
        expectedError.toString(),
      )
      expect(
        screen.queryByText(errorMessages.TEXT_CHECK_FAILED.title),
      ).not.toBeInTheDocument()

      const routerSpy = vi.spyOn(router, "push")

      const button = screen.getByRole("button", { name: "Rubriken bearbeiten" })

      await user.click(button)

      expect(routerSpy).toHaveBeenCalledOnce()
    })

    it("displays success message when no text mistakes are found", async () => {
      const expectedError = 0

      vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
        status: 200,
        data: {
          suggestions: [],
          totalTextCheckErrors: expectedError,
          categoryTypes: [],
        },
      } as ServiceResponse<TextCheckAllResponse>)

      await renderComponent(Kind.PENDING_PROCEEDING)

      await flushPromises()

      expect(
        screen.queryByText(errorMessages.TEXT_CHECK_FAILED.title),
      ).not.toBeInTheDocument()

      expect(
        screen.getByText("Es wurden keine Rechtschreibfehler identifiziert."),
      ).toBeInTheDocument()
    })
  })

  it("error in text check service call shows error info modal", async () => {
    vi.spyOn(languageToolService, "checkAll").mockResolvedValue({
      status: 400,
      error: {
        title: "error title for text check service",
        description: "error description for text check service",
      },
    } as ServiceResponse<TextCheckAllResponse>)

    await renderComponent(Kind.PENDING_PROCEEDING)

    await flushPromises()

    expect(screen.getByText("error title for text check service")).toBeVisible()

    expect(
      screen.getByText("error description for text check service"),
    ).toBeVisible()
  })
})
