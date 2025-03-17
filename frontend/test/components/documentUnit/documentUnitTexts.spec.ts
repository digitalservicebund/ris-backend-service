import { createTestingPinia } from "@pinia/testing"
import { render, screen, within } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import type { Component } from "vue"
import { ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitTexts from "@/components/texts/DocumentUnitTexts.vue"
import DocumentUnit, {
  longTextLabels,
  LongTexts,
  shortTextLabels,
  ShortTexts,
} from "@/domain/documentUnit"
import ParticipatingJudge from "@/domain/participatingJudge"
import routes from "~/test-helper/routes"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

async function renderComponent(shortTexts?: ShortTexts, longTexts?: LongTexts) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  const textEditorRefs = ref<Record<string, Component | null>>({})
  const registerTextEditorRef = (key: string, el: Component | null) => {
    if (el) {
      textEditorRefs.value[key] = el
    }
  }

  const utils = render(DocumentUnitTexts, {
    props: {
      registerTextEditorRef: registerTextEditorRef,
    },
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
        [router],
      ],
    },
  })

  await flushPromises()

  return { ...utils, textEditorRefs }
}

describe("Texts", () => {
  test("renders texts subheadings", async () => {
    await renderComponent()
    expect(screen.getByText("Kurztexte")).toBeVisible()
    expect(screen.getByText("Langtexte")).toBeVisible()
    expect(screen.getByText("Weitere Langtexte")).toBeVisible()
  })

  test("renders all text categories as buttons", async () => {
    await renderComponent()
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
      screen.getByRole("button", { name: "Mitwirkende Richter" }),
    ).toBeVisible()
    expect(
      screen.getByRole("button", { name: "Sonstiger Langtext" }),
    ).toBeVisible()
    expect(screen.getByRole("button", { name: "Gliederung" })).toBeVisible()
  })

  test("renders all text categories as text fields", async () => {
    await renderComponent(
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
        participatingJudges: [
          new ParticipatingJudge({
            name: "participating judges",
          }),
        ],
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
    expect(screen.getByLabelText("Mitwirkende Richter")).toBeVisible()
    expect(
      screen.getByLabelText("Sonstiger Langtext Button Leiste"),
    ).toBeVisible()
    expect(screen.getByLabelText("Gliederung Button Leiste")).toBeVisible()
  }, 10000)

  test("renders all tiptap text editors with ref and text check button", async () => {
    const { textEditorRefs } = await renderComponent(
      {
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
        otherLongText: "otherLongText",
        outline: "outline",
      },
    )

    const excludeLabels = [
      "decisionName",
      "dissentingOpinion",
      "participatingJudges",
    ]

    Object.keys({ ...shortTextLabels, ...longTextLabels })
      .filter((category) => !excludeLabels.includes(category))
      .forEach((category) => {
        if (!textEditorRefs.value[category]) {
          throw new Error(`Category '${category}' not found in textEditorRefs.`)
        }
        expect(textEditorRefs.value[category]).toBeTruthy()

        const editor = screen.getByTestId(category)
        expect(
          within(editor).getByLabelText("Rechtschreibprüfung"),
          `Category '${category}' should have text check button`,
        ).toBeInTheDocument()
      })
  })
})
