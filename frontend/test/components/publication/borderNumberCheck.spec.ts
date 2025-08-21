import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import BorderNumberCheck from "@/components/publication/BorderNumberCheck.vue"
import {
  BorderNumberLinkValidationResult,
  BorderNumberValidationResult,
} from "@/services/borderNumberService"
import routes from "~pages"

describe("BorderNumberCheck", () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.useRealTimers()
  })

  describe("Valid border numbers and links", () => {
    it("should emit valid when no errors are reported", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      const { emitted } = await renderComponent()

      expect(emitted("borderNumberValidationUpdated")).toEqual([[true]])
    })

    it("should show the success message", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(
        await screen.findByText("Die Reihenfolge der Randnummern ist korrekt."),
      ).toBeVisible()
    })

    it("should not show error messages", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(
        screen.queryByText(
          "Die Reihenfolge der Randnummern ist nicht korrekt.",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          "Es gibt ungültige Randnummern-Verweise in folgenden Rubriken:",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          /Bei der Randnummernprüfung ist ein Fehler aufgetreten./,
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText("Die Randnummern werden neu berechnet"),
      ).not.toBeInTheDocument()
    })
  })

  describe("Invalid border numbers", () => {
    it("should emit invalid with border number error", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: false,
        invalidCategory: "reasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      const { emitted } = await renderComponent()

      expect(emitted("borderNumberValidationUpdated")).toEqual([[false]])
    })

    it("should show error message", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: false,
        invalidCategory: "reasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(
        await screen.findByText(
          "Die Reihenfolge der Randnummern ist nicht korrekt.",
        ),
      ).toBeVisible()
    })

    it("should show link to the affected category", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: false,
        invalidCategory: "decisionReasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      const { router } = await renderComponent()
      const routerSpy = vi.spyOn(router, "push").mockImplementation(vi.fn())

      await fireEvent.click(screen.getByText("Entscheidungsgründe"))

      expect(scrollIntoViewportByIdMock).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledWith({
        name: "caselaw-documentUnit-documentNumber-categories",
      })
    })

    it("should show actual and expected border numbers", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: false,
        invalidCategory: "reasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(await screen.findByText("Erwartete Randnummer")).toBeVisible()
      expect(await screen.findByText("3")).toBeVisible()
      expect(await screen.findByText("Tatsächliche Randnummer")).toBeVisible()
      expect(await screen.findByText("4")).toBeVisible()
    })

    it("should allow to recalculate border numbers", async () => {
      validateBorderNumbersMock.mockReturnValueOnce({
        isValid: false,
        hasError: false,
        invalidCategory: "reasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      // After the recalc action, the result is valid
      validateBorderNumbersMock.mockReturnValueOnce({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })
      // Recalc action has a fake delay
      vi.useFakeTimers()

      const { emitted } = await renderComponent()

      const recalcButton = await screen.findByText("Randnummern neu berechnen")
      expect(recalcButton).toBeVisible()

      await fireEvent.click(recalcButton)

      expect(
        await screen.findByText("Die Randnummern werden neu berechnet"),
      ).toBeVisible()
      expect(
        screen.queryByText("Die Reihenfolge der Randnummern ist korrekt."),
      ).not.toBeInTheDocument()
      expect(makeBorderNumbersSequentialMock).toHaveBeenCalledOnce()
      // emit has no params
      expect(emitted("borderNumbersRecalculated")).toEqual([[]])

      vi.advanceTimersByTime(3_000)

      expect(
        await screen.findByText("Die Reihenfolge der Randnummern ist korrekt."),
      ).toBeVisible()
    })

    it("should handle error in border number check", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: true,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(
        await screen.findByText(
          /Bei der Randnummernprüfung ist ein Fehler aufgetreten./,
        ),
      ).toBeVisible()
      expect(
        screen.queryByText("Randnummern neu berechnen"),
      ).not.toBeInTheDocument()
    })

    it("should not show success message or irrelevant error messages", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: false,
        hasError: false,
        invalidCategory: "reasons",
        expectedBorderNumber: 3,
        firstInvalidBorderNumber: "4",
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: true,
      })

      await renderComponent()

      expect(
        screen.queryByText("Die Reihenfolge der Randnummern ist korrekt."),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          "Es gibt ungültige Randnummern-Verweise in folgenden Rubriken:",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          /Bei der Randnummernprüfung ist ein Fehler aufgetreten./,
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText("Die Randnummern werden neu berechnet"),
      ).not.toBeInTheDocument()
    })
  })

  describe("Invalid border number links", () => {
    it("should emit invalid with invalid border number links", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: false,
        invalidCategories: ["reasons"],
      })

      const { emitted } = await renderComponent()

      expect(emitted("borderNumberValidationUpdated")).toEqual([[false]])
    })

    it("should show error message", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: false,
        invalidCategories: ["reasons"],
      })

      await renderComponent()

      expect(
        await screen.findByText(
          "Es gibt ungültige Randnummern-Verweise in folgenden Rubriken:",
        ),
      ).toBeVisible()
    })

    it("should show affected categories", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: false,
        invalidCategories: ["reasons", "headnote"],
      })

      await renderComponent()

      expect(await screen.findByText("Gründe")).toBeVisible()
      expect(await screen.findByText("Orientierungssatz")).toBeVisible()
    })

    it("should show link to the affected category", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: false,
        invalidCategories: ["reasons"],
      })

      const { router } = await renderComponent()
      const routerSpy = vi.spyOn(router, "push").mockImplementation(vi.fn())

      await fireEvent.click(screen.getByText("Gründe"))

      expect(scrollIntoViewportByIdMock).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledOnce()
      expect(routerSpy).toHaveBeenCalledWith({
        name: "caselaw-documentUnit-documentNumber-categories",
      })
    })

    it("should not show success message or irrelevant error messages", async () => {
      validateBorderNumbersMock.mockReturnValue({
        isValid: true,
        hasError: false,
      })
      validateBorderNumberLinksMock.mockReturnValue({
        isValid: false,
        invalidCategories: ["reasons"],
      })

      await renderComponent()

      expect(
        screen.queryByText("Die Reihenfolge der Randnummern ist korrekt."),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          "Die Reihenfolge der Randnummern ist nicht korrekt.",
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText(
          /Bei der Randnummernprüfung ist ein Fehler aufgetreten./,
        ),
      ).not.toBeInTheDocument()

      expect(
        screen.queryByText("Die Randnummern werden neu berechnet"),
      ).not.toBeInTheDocument()
    })
  })
})

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
    ...render(BorderNumberCheck, { global: { plugins: [router] } }),
  }
}

const scrollIntoViewportByIdMock = vi.fn()
vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: scrollIntoViewportByIdMock,
  }),
}))

const {
  validateBorderNumberLinksMock,
  validateBorderNumbersMock,
  makeBorderNumbersSequentialMock,
} = vi.hoisted(() => {
  return {
    validateBorderNumbersMock: vi.fn<() => BorderNumberValidationResult>(),
    validateBorderNumberLinksMock:
      vi.fn<() => BorderNumberLinkValidationResult>(),
    makeBorderNumbersSequentialMock: vi.fn(),
  }
})
vi.mock("@/services/borderNumberService", () => {
  return {
    default: {
      validateBorderNumbers: validateBorderNumbersMock,
      validateBorderNumberLinks: validateBorderNumberLinksMock,
      makeBorderNumbersSequential: makeBorderNumbersSequentialMock,
    },
  }
})
