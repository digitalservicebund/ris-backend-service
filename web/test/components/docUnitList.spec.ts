import { createTestingPinia } from "@pinia/testing"
import { fireEvent } from "@testing-library/dom"
import { render, screen } from "@testing-library/vue"
import { describe, test, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnitList from "../../src/components/DocUnitList.vue"
import { buildEmptyDocUnit } from "../../src/types/DocUnit"

describe("docUnit list", () => {
  const pinia = createTestingPinia({ stubActions: false })
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "Dokumente",
        component: {},
      },
    ],
  })

  test("renders fallback if no docUnits found", async () => {
    render(DocUnitList, {
      props: {
        docUnits: [],
      },
      global: {
        plugins: [pinia, vuetify, router],
      },
    })

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders docUnits", async () => {
    const docUnit = buildEmptyDocUnit()
    docUnit.id = "1"
    docUnit.aktenzeichen = "foo"

    render(DocUnitList, {
      props: {
        docUnits: [docUnit],
      },
      global: {
        plugins: [pinia, vuetify, router],
      },
    })

    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).toBeNull()
  })

  test("delete emits event", async () => {
    const docUnit = buildEmptyDocUnit()
    docUnit.id = "1"
    docUnit.aktenzeichen = "foo"

    const { emitted } = render(DocUnitList, {
      props: {
        docUnits: [docUnit],
      },
      global: {
        plugins: [pinia, vuetify, router],
      },
    })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    expect(emitted().deleteDocUnit).toBeTruthy()
  })
})
