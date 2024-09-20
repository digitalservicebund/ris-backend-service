import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import DocumentUnitTextsNew from "@/components/texts/DocumentUnitTexts.vue"
import DocumentUnit, { LongTexts, ShortTexts } from "@/domain/documentUnit"

function renderComponent(shortTexts?: ShortTexts, longTexts?: LongTexts) {
  return render(DocumentUnitTextsNew, {
    global: {
      plugins: [
        [
          createTestingPinia({
            initialState: {
              session: { user: { roles: ["Internal"] } },
              docunitStore: {
                documentUnit: new DocumentUnit("foo", {
                  documentNumber: "1234567891234",
                  shortTexts: shortTexts ?? {},
                  longTexts: longTexts ?? {},
                }),
              },
            },
          }),
        ],
      ],
    },
  })
}

describe("Texts", () => {
  test("renders texts subheadings", async () => {
    renderComponent()
    expect(screen.getByText("Kurztexte")).toBeVisible()
    expect(screen.getByText("Langtexte")).toBeVisible()
    expect(screen.getByText("Weitere Langtexte")).toBeVisible()
  })

  test("renders all text categories as buttons", async () => {
    renderComponent()
    expect(
      screen.getByRole("button", { name: "Entscheidungsname" }),
    ).toBeVisible()
    expect(screen.getByRole("button", { name: "Titelzeile" })).toBeVisible()
    expect(screen.getByRole("button", { name: "Leitsatz" })).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Orientierungssatz" }),
    ).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Sonstiger Orientierungssatz" }),
    ).toBeVisible()
    expect(screen.getByRole("button", { name: "Tenor" })).toBeVisible()
    expect(screen.getByRole("button", { name: "Gründe" })).toBeVisible()
    expect(screen.getByRole("button", { name: "Tatbestand" })).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Entscheidungsgründe" }),
    ).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Abweichende Meinung" }),
    ).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Sonstiger Langtext" }),
    ).toBeVisible()
    expect(screen.getByRole("button", { name: "Gliederung" })).toBeVisible()
  })

  test("renders all text categories as text fields", async () => {
    renderComponent(
      {
        decisionName: "decision name",
        headline: "headline",
        guidingPrinciple: "guiding principle",
        headnote: "headnote",
        otherHeadnote: "other headnote",
      },
      {
        tenor: "tenor",
        reasons: "reasons",
        caseFacts: "case facts",
        decisionReasons: "decision reasons",
        dissentingOpinion: "dissenting opinion",
        otherLongText: "other long text",
        outline: "outline",
      },
    )
    expect(
      screen.getByRole("textbox", { name: "Entscheidungsname" }),
    ).toBeVisible()
    expect(screen.getByLabelText("Titelzeile Button Leiste")).toBeVisible()
    expect(screen.getByLabelText("Leitsatz Button Leiste")).toBeVisible()
    expect(
      screen.getByLabelText("Orientierungssatz Button Leiste"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Sonstiger Orientierungssatz Button Leiste"),
    ).toBeVisible()
    expect(screen.getByLabelText("Tenor Button Leiste")).toBeVisible()
    expect(screen.getByLabelText("Gründe Button Leiste")).toBeVisible()
    expect(screen.getByLabelText("Tatbestand Button Leiste")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsgründe Button Leiste"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Abweichende Meinung Button Leiste"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Sonstiger Langtext Button Leiste"),
    ).toBeVisible()
    expect(screen.getByLabelText("Gliederung Button Leiste")).toBeVisible()
  })
})
