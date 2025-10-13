import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { beforeAll } from "vitest"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewShortTexts from "@/components/preview/PreviewShortTexts.vue"
import { ShortTexts } from "@/domain/decision"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

beforeAll(() => {
  useFeatureToggleServiceMock()
})

function renderComponent(shortTexts: ShortTexts) {
  return render(PreviewShortTexts, {
    props: {
      shortTexts: shortTexts,
      validBorderNumbers: [],
    },
    global: {
      plugins: [createTestingPinia()],
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}

describe("preview short texts", () => {
  test("renders all short texts", async () => {
    renderComponent({
      decisionNames: ["decision name"],
      headline: "headline",
      guidingPrinciple: "guiding principle",
      headnote: "headnote",
      otherHeadnote: "other headnote",
    })

    expect(await screen.findByText("Entscheidungsnamen")).toBeInTheDocument()
    expect(await screen.findByText("Titelzeile")).toBeInTheDocument()
    expect(await screen.findByText("Leitsatz")).toBeInTheDocument()
    expect(await screen.findByText("Orientierungssatz")).toBeInTheDocument()
    expect(
      await screen.findByText("Sonstiger Orientierungssatz"),
    ).toBeInTheDocument()
  })

  it.each([
    [
      "Entscheidungsnamen",
      { decisionNames: ["decisionName"] },
      [
        "Titelzeile",
        "Leitsatz",
        "Orientierungssatz",
        "Sonstiger Orientierungssatz",
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
      ],
    ],
    [
      "Sonstiger Orientierungssatz",
      { otherHeadnote: "other headnote" },
      ["Entscheidungsname", "Titelzeile", "Leitsatz", "Orientierungssatz"],
    ],
  ])(
    `renders %s and nothing else`,
    async (expected: string, texts: ShortTexts, notExpected: string[]) => {
      renderComponent(texts)
      expect(await screen.findByText(expected)).toBeInTheDocument()
      notExpected.forEach((it) => {
        expect(screen.queryByText(it)).not.toBeInTheDocument()
      })
    },
  )
})
