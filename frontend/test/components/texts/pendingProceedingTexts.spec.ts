import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import type { Component } from "vue"
import { nextTick, ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingTexts from "@/components/texts/PendingProceedingTexts.vue"
import admissionOfAppealTypes from "@/data/admissionOfAppealTypes.json"
import appellantTypes from "@/data/appellantTypes.json"
import { ShortTexts } from "@/domain/documentUnit"
import PendingProceeding, {
  pendingProceedingLabels,
} from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

async function renderComponent(
  shortTexts?: ShortTexts,
  otherTexts?: {
    legalIssue?: string
    appellant?: string
    admissionOfAppeal?: string
    resolutionNote?: string
    courtLabel?: string
  },
) {
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

  const utils = render(PendingProceedingTexts, {
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
                documentUnit: new PendingProceeding("foo", {
                  documentNumber: "1234567891234",
                  shortTexts: shortTexts ?? {},
                  legalIssue: otherTexts?.legalIssue,
                  appellant: otherTexts?.appellant,
                  admissionOfAppeal: otherTexts?.admissionOfAppeal,
                  resolutionNote: otherTexts?.resolutionNote,
                  coreData: {
                    court: { label: otherTexts?.courtLabel ?? "" },
                  },
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
  const docUnitStoreInstance = useDocumentUnitStore()

  return { ...utils, textEditorRefs, docUnitStoreInstance }
}

describe("Pending proceeding texts", () => {
  test("renders text subheading", async () => {
    await renderComponent()
    expect(screen.getByText("Kurztexte")).toBeVisible()
  })

  test("renders all text categories as buttons", async () => {
    await renderComponent()
    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.headline,
      }),
    ).toBeVisible()
    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.legalIssue,
      }),
    ).toBeVisible()

    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.resolutionNote,
      }),
    ).toBeVisible()
  })

  test("renders all text categories as text fields", async () => {
    await renderComponent(
      {
        headline: "Titelzeile",
      },
      {
        legalIssue: "Rechtsfrage Test",
        appellant: appellantTypes.items[0].label,
        admissionOfAppeal: admissionOfAppealTypes.items[0].label,
        resolutionNote: "Erledigungsvermerk Test",
      },
    )
    expect(
      screen.getByText(pendingProceedingLabels.legalIssue, {
        exact: true,
      }),
    ).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.legalIssue + " Test", {
        exact: true,
      }),
    ).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.appellant, {
        exact: true,
      }),
    ).toBeVisible()
    expect(screen.getByText(appellantTypes.items[0].label)).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.admissionOfAppeal, {
        exact: true,
      }),
    ).toBeVisible()
    expect(
      screen.getByText(admissionOfAppealTypes.items[0].label),
    ).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.resolutionNote, {
        exact: true,
      }),
    ).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.resolutionNote + " Test", {
        exact: true,
      }),
    ).toBeVisible()
  })
  test("headline remains empty on initial load if no court is set", async () => {
    const { docUnitStoreInstance } = await renderComponent({}, {})

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      undefined,
    )
  })

  test("headline is not auto-generated on initial load if manually edited", async () => {
    const manualHeadline = "My manually written headline"
    const { docUnitStoreInstance } = await renderComponent(
      {
        headline: manualHeadline,
      },
      { courtLabel: "Amtsgericht Berlin" },
    )

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      manualHeadline,
    )
  })

  test("headline updates automatically when court is selected", async () => {
    const { docUnitStoreInstance } = await renderComponent({
      headline: "initial Headline",
    })

    const expectedHeadline = "Anhängiges Verfahren beim Landgericht München"

    docUnitStoreInstance.documentUnit!.coreData!.court!.label =
      "Landgericht München"
    await nextTick()

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      expectedHeadline,
    )
  })

  test("manual headline is overwritten when court changes after manual edit", async () => {
    const customHeadline = "Ein Fall für das Gericht"
    const initialCourt = "Amtsgericht Hamburg"
    const newCourt = "Oberlandesgericht Celle"

    const { docUnitStoreInstance } = await renderComponent(
      { headline: customHeadline },
      { courtLabel: initialCourt },
    )

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      customHeadline,
    )

    docUnitStoreInstance.documentUnit!.coreData.court!.label = newCourt
    await nextTick()

    const expectedAutoHeadline = "Anhängiges Verfahren beim " + newCourt
    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      expectedAutoHeadline,
    )
  })

  test("headline stays when court is cleared", async () => {
    const initalCourtHeadline = "Initial Court Headline"
    const { docUnitStoreInstance } = await renderComponent(
      { headline: initalCourtHeadline },
      { courtLabel: "Bundesgerichtshof" },
    )

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      "Initial Court Headline",
    )

    docUnitStoreInstance.documentUnit!.coreData!.court!.label = ""
    await nextTick()

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      "Initial Court Headline",
    )
  })
})
