import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import type { Component } from "vue"
import { nextTick, ref } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingTexts from "@/components/texts/PendingProceedingTexts.vue"
import admissionOfAppealTypes from "@/data/admissionOfAppealTypes.json"
import appellantTypes from "@/data/appellantTypes.json"
import { Court } from "@/domain/court"
import PendingProceeding, {
  pendingProceedingLabels,
  PendingProceedingShortTexts,
} from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import routes from "~/test-helper/routes"

async function renderComponent(
  shortTexts?: PendingProceedingShortTexts,
  court?: Court,
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
              session: { user: { internal: true } },
              docunitStore: {
                documentUnit: new PendingProceeding("foo", {
                  documentNumber: "1234567891234",
                  shortTexts: shortTexts ?? {},
                  coreData: {
                    court: court ?? { label: "" },
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

  test("renders all non-required text categories as buttons", async () => {
    await renderComponent()
    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.headline,
      }),
    ).toBeVisible()

    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.resolutionNote,
      }),
    ).toBeVisible()
  })

  test("renders required text category directly", async () => {
    await renderComponent()

    expect(
      screen.getByTestId(pendingProceedingLabels.legalIssue + " *"),
    ).toBeVisible()
  })

  test("renders all categories with data", async () => {
    await renderComponent({
      headline: "Titelzeile Test",
      legalIssue: "Rechtsfrage Test",
      appellant: appellantTypes.items[0].label,
      admissionOfAppeal: admissionOfAppealTypes.items[0].label,
      resolutionNote: "Erledigungsvermerk Test",
    })
    expect(
      screen.getByText(pendingProceedingLabels.headline, {
        exact: true,
      }),
    ).toBeVisible()
    expect(
      screen.getByText(pendingProceedingLabels.headline + " Test", {
        exact: true,
      }),
    ).toBeVisible()

    expect(
      screen.getByText(pendingProceedingLabels.legalIssue + " *", {
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

  test("headline remains empty on initial load", async () => {
    const { docUnitStoreInstance } = await renderComponent({})

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      undefined,
    )
    expect(
      screen.getByRole("button", {
        name: pendingProceedingLabels.headline,
      }),
    ).toBeVisible()
  })

  test("headline is not auto-generated on initial load", async () => {
    const manualHeadline = "My manually written headline"
    await renderComponent(
      {
        headline: manualHeadline,
      },
      { label: "Amtsgericht Berlin" },
    )

    expect(screen.getByText(manualHeadline)).toBeVisible()
  })

  test("headline is auto-generated when court is selected", async () => {
    const { docUnitStoreInstance } = await renderComponent({
      headline: "initial Headline",
    })

    const expectedHeadline = "Anhängiges Verfahren beim Landgericht München"

    docUnitStoreInstance.documentUnit!.coreData!.court!.label =
      "Landgericht München"
    await nextTick()

    expect(screen.getByText(expectedHeadline)).toBeVisible()
  })

  test("headline is not cleared when court is cleared", async () => {
    const initialHeadline = "Initial Headline"
    const { docUnitStoreInstance } = await renderComponent(
      { headline: initialHeadline },
      { label: "BGH" },
    )

    expect(docUnitStoreInstance.documentUnit?.shortTexts.headline).toBe(
      initialHeadline,
    )

    docUnitStoreInstance.documentUnit!.coreData!.court!.label = ""
    await nextTick()

    expect(screen.getByText(initialHeadline)).toBeVisible()
  })
})
