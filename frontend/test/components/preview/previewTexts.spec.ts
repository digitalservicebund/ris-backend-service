import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewTexts from "@/components/preview/PreviewShortTexts.vue"
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
      otherHeadnote: "other headnote",
      tenor: "tenor",
      reasons: "reasons",
      caseFacts: "casefacts",
      decisionReasons: "decision reasons",
      dissentingOpinion: "dissenting opinion",
      otherLongText: "other long text",
      outline: "outline",
    })

    expect(await screen.findByText("Entscheidungsname")).toBeInTheDocument()
    expect(await screen.findByText("Titelzeile")).toBeInTheDocument()
    expect(await screen.findByText("Leitsatz")).toBeInTheDocument()
    expect(await screen.findByText("Orientierungssatz")).toBeInTheDocument()
    expect(
      await screen.findByText("Sonstiger Orientierungssatz"),
    ).toBeInTheDocument()
    expect(await screen.findByText("Tenor")).toBeInTheDocument()
    expect(await screen.findByText("Gründe")).toBeInTheDocument()
    expect(await screen.findByText("Tatbestand")).toBeInTheDocument()
    expect(await screen.findByText("Entscheidungsgründe")).toBeInTheDocument()
    expect(await screen.findByText("Abweichende Meinung")).toBeInTheDocument()
    expect(await screen.findByText("Sonstiger Langtext")).toBeInTheDocument()
    expect(await screen.findByText("Gliederung")).toBeInTheDocument()
  })

  it.each([
    [
      "Entscheidungsname",
      { decisionName: "decisionName" },
      [
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
      ],
    ],
    [
      "Titelzeile",
      { headline: "headline" },
      [
        "Entscheidungsname",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
      ],
    ],
    [
      "Leitsatz",
      { guidingPrinciple: "guidingPrinciple" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
      ],
    ],
    [
      "Orientierungssatz",
      { headnote: "headnote" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
      ],
    ],
    [
      "Sonstiger Orientierungssatz",
      { otherHeadnote: "other headnote" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
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
        "Sonstiger Orientierungssatz",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
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
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
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
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
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
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Abweichende Meinung",
        "Sonstiger Langtext",
        "Gliederung",
      ],
    ],
    [
      "Sonstiger Langtext",
      { otherLongText: "other long text" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Gliederung",
      ],
    ],
    [
      "Gliederung",
      { outline: "outline" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Abweichende Meinung",
        "Sonstiger Langtext",
      ],
    ],
    [
      "Abweichende Meinung",
      { dissentingOpinion: "dissenting opinion" },
      [
        "Entscheidungsname",
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
        "Tenor",
        "Gründe",
        "Tatbestand",
        "Entscheidungsgründe",
        "Sonstiger Langtext",
        "Gliederung",
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
