import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewTexts from "@/components/preview/PreviewTexts.vue"
import { Texts } from "@/domain/documentUnit"

function renderComponent(texts: Texts) {
  return render(PreviewTexts, {
    props: {
      texts: texts,
      validBorderNumbers: [],
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}
describe("preview texts", () => {
  test("renders all texts", async () => {
    renderComponent({
      decisionName: "decision name",
      headline: "headline",
      guidingPrinciple: "guiding principle",
      headnote: "headnote",
      tenor: "tenor",
      reasons: "reasons",
      caseFacts: "casefacts",
      decisionReasons: "decision reasons",
    })

    expect(await screen.findByText("Entscheidungsname")).toBeInTheDocument()
    expect(await screen.findByText("Titelzeile")).toBeInTheDocument()
    expect(await screen.findByText("Leitsatz")).toBeInTheDocument()
    expect(await screen.findByText("Orientierungssatz")).toBeInTheDocument()
    expect(await screen.findByText("Tenor")).toBeInTheDocument()
    expect(await screen.findByText("Gründe")).toBeInTheDocument()
    expect(await screen.findByText("Tatbestand")).toBeInTheDocument()
    expect(await screen.findByText("Entscheidungsgründe")).toBeInTheDocument()
  })

  it.each([
    [
      "Entscheidungsname",
      { decisionName: "decisionName" },
      [
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Titelzeile",
      { headline: "headline" },
      [
        "Entscheidungsname",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Leitsatz",
      { guidingPrinciple: "guidingPrinciple" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Orientierungssatz",
      { headnote: "headnote" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Tenor",
      { tenor: "tenor" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Gründe",
      { reasons: "reasons" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Tatbestand",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Tatbestand",
      { caseFacts: "caseFacts" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Entscheidungsgründe",
      ],
    ],
    [
      "Entscheidungsgründe",
      { decisionReasons: "decisionReasons" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
      ],
    ],
  ])(
    `renders %s and nothing else`,
    async (expected: string, texts: Texts, notExpected: string[]) => {
      renderComponent(texts)
      expect(await screen.findByText(expected)).toBeInTheDocument()
      notExpected.forEach((it) => {
        expect(screen.queryByText(it)).not.toBeInTheDocument()
      })
    },
  )
})
