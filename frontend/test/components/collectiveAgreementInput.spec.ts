import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { beforeEach, vi } from "vitest"
import CollectiveAgreementInput from "@/components/CollectiveAgreementInput.vue"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import { SingleNormValidationInfo } from "@/domain/singleNorm"

const server = setupServer(
  http.get("/api/v1/caselaw/collective-agreement-industries", () => {
    const industry: CollectiveAgreementIndustry = {
      id: "4512f151-6b7f-4080-bb07-91b16877a510",
      label:
        "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
    }
    return HttpResponse.json([industry])
  }),
  http.post(
    "/api/v1/caselaw/documentunits/validateSingleNorm",
    async (request) => {
      if (
        ((await request.request.json()) as SingleNormValidationInfo)
          .singleNorm === "invalid single norm"
      ) {
        return HttpResponse.text("Validation error")
      }

      return HttpResponse.text("Ok")
    },
  ),
)

function renderComponent(options?: { modelValue?: CollectiveAgreement }) {
  const user = userEvent.setup()
  const utils = render(CollectiveAgreementInput, {
    props: {
      modelValue: new CollectiveAgreement({ ...options?.modelValue }),
    },
  })
  return { user, ...utils }
}

describe("Collective Agreement Input", () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })
  beforeAll(() => {
    server.listen()
  })
  afterAll(() => {
    server.close()
  })

  it("should disable 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByRole("textbox", { name: "Bezeichnung des Tarifvertrags" }),
    ).toBeInTheDocument()
    expect(screen.getByRole("textbox", { name: "Datum" })).toBeInTheDocument()
    expect(
      screen.getByRole("textbox", { name: "Tarifnorm" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("combobox", { name: "Branche" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeDisabled()
  })

  it("should disable 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {} as CollectiveAgreement,
    })
    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeDisabled()
  })

  it("should disable 'Übernehmen' button with invalid date input", async () => {
    // Arrange + Act
    const { user } = renderComponent({
      modelValue: {
        id: "73b51cc0-c779-4c31-954a-0cc74943d6d4",
        name: "Stehende Bühnen",
      } as CollectiveAgreement,
    })
    const dateInput = screen.getByRole("textbox", { name: "Datum" })

    await user.type(dateInput, "test")
    await user.tab()

    expect(
      screen.getByText(
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      ),
    ).toBeVisible()

    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeDisabled()

    await user.clear(dateInput)
    await user.type(dateInput, "2025")
    await user.tab()

    expect(
      screen.queryByText(
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      ),
    ).not.toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeEnabled()
  })

  it("should disable 'Übernehmen' button with invalid norm input", async () => {
    const { user } = renderComponent({
      modelValue: {
        id: "73b51cc0-c779-4c31-954a-0cc74943d6d4",
        name: "Stehende Bühnen",
      } as CollectiveAgreement,
    })
    const normInput = screen.getByRole("textbox", { name: "Tarifnorm" })

    await user.type(normInput, "invalid single norm")
    await user.tab()

    expect(screen.getByText("Inhalt nicht valide")).toBeVisible()

    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeDisabled()

    await user.clear(normInput)
    await user.type(normInput, "§ 1")
    await user.tab()

    expect(screen.queryByText("Inhalt nicht valide")).not.toBeInTheDocument()

    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeEnabled()
  })

  it("should enable 'Übernehmen' button with complete and valid input", async () => {
    renderComponent({
      modelValue: {
        id: "73b51cc0-c779-4c31-954a-0cc74943d6d4",
        industry: {
          id: "4512f151-6b7f-4080-bb07-91b16877a510",
          label:
            "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
        } as CollectiveAgreementIndustry,
        name: "Stehende Bühnen",
        date: "12.2001",
        norm: "§ 23",
      } as CollectiveAgreement,
    })

    expect(
      screen.getByRole("textbox", { name: "Bezeichnung des Tarifvertrags" }),
    ).toHaveValue("Stehende Bühnen")
    expect(screen.getByRole("textbox", { name: "Datum" })).toHaveValue(
      "12.2001",
    )
    expect(screen.getByRole("textbox", { name: "Tarifnorm" })).toHaveValue(
      "§ 23",
    )
    expect(screen.getByRole("combobox", { name: "Branche" })).toHaveValue(
      "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
    )

    expect(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    ).toBeEnabled()
  })

  it("should emit cancelEdit event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "73b51cc0-c779-4c31-954a-0cc74943d6d4",
        name: "Stehende Bühnen",
      } as CollectiveAgreement,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Abbrechen" }))

    // Assert
    expect(emitted("cancelEdit")).toBeTruthy()
  })

  it("should emit removeEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "73b51cc0-c779-4c31-954a-0cc74943d6d4",
        name: "Stehende Bühnen",
      } as CollectiveAgreement,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })

  it("should validate date format", async () => {
    const { user } = renderComponent({
      modelValue: {} as CollectiveAgreement,
    })

    const dateInput = screen.getByRole("textbox", { name: "Datum" })

    await user.type(dateInput, "test")
    await user.tab()

    expect(
      screen.getByText(
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      ),
    ).toBeVisible()

    await user.clear(dateInput)
    await user.type(dateInput, "12.2001")
    await user.tab()

    expect(
      screen.queryByText(
        "Datum entspricht nicht dem erlaubten Muster (TT.MM.JJJJ, MM.JJJJ oder JJJJ)",
      ),
    ).not.toBeInTheDocument()

    expect(dateInput).toHaveValue("12.2001")
  })

  const testCases = [
    { invalidDate: "9999", validDate: "2001" },
    { invalidDate: "12.9999", validDate: "12.2001" },
    { invalidDate: "31.12.9999", validDate: "31.12.2001" },
  ]
  it.each(testCases)(
    "should validate date is not in future",
    async ({ invalidDate, validDate }) => {
      const { user } = renderComponent({
        modelValue: {} as CollectiveAgreement,
      })

      const dateInput = screen.getByRole("textbox", { name: "Datum" })

      await user.type(dateInput, invalidDate)
      await user.tab()

      expect(
        screen.getByText("Das Datum darf nicht in der Zukunft liegen"),
      ).toBeVisible()

      await user.clear(dateInput)
      await user.type(dateInput, validDate)
      await user.tab()

      expect(
        screen.queryByText("Das Datum darf nicht in der Zukunft liegen"),
      ).not.toBeInTheDocument()

      expect(dateInput).toHaveValue(validDate)
    },
  )

  it("should validate norm", async () => {
    const { user } = renderComponent({
      modelValue: {} as CollectiveAgreement,
    })

    const normInput = screen.getByRole("textbox", { name: "Tarifnorm" })

    await user.type(normInput, "invalid single norm")
    await user.tab()

    expect(screen.getByText("Inhalt nicht valide")).toBeVisible()

    await user.clear(normInput)
    await user.type(normInput, "§ 23")
    await user.tab()

    expect(screen.queryByText("Inhalt nicht valide")).not.toBeInTheDocument()

    expect(normInput).toHaveValue("§ 23")
  })

  it("should input values", async () => {
    const { user, emitted } = renderComponent({
      modelValue: {} as CollectiveAgreement,
    })

    await user.type(
      screen.getByRole("textbox", { name: "Bezeichnung des Tarifvertrags" }),
      "Stehende Bühnen",
    )
    await user.type(screen.getByRole("textbox", { name: "Datum" }), "12.2001")
    await user.type(screen.getByRole("textbox", { name: "Tarifnorm" }), "§ 23")
    await user.type(screen.getByRole("combobox", { name: "Branche" }), "Ei")
    await user.click(
      screen.getByText(
        "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
      ),
    )

    await user.click(
      screen.getByRole("button", { name: "Tarifvertrag speichern" }),
    )

    expect(emitted("addEntry")).toBeTruthy()

    const collectiveAgreement = (
      emitted("update:modelValue").findLast(() => true) as [CollectiveAgreement]
    )[0]

    expect(collectiveAgreement.name).toEqual("Stehende Bühnen")
    expect(collectiveAgreement.date).toEqual("12.2001")
    expect(collectiveAgreement.norm).toEqual("§ 23")
    expect(collectiveAgreement.industry?.label).toEqual(
      "Eisen-, Stahl-, Metall-, und Elektroindustrie, Metallverarbeitung",
    )
    expect(collectiveAgreement.industry?.id).toEqual(
      "4512f151-6b7f-4080-bb07-91b16877a510",
    )
  })
})
