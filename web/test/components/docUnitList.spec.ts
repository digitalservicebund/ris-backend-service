import { fireEvent } from "@testing-library/dom"
import { render, screen } from "@testing-library/vue"
import { describe, test, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnitList from "../../src/components/DocUnitList.vue"
import DocUnit from "@/domain/docUnit"

describe("docUnit list", () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-docUnit-:documentNumber-files",
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
        plugins: [vuetify, router],
      },
    })

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders docUnits", async () => {
    const docUnit = new DocUnit("1", { aktenzeichen: "foo" })

    render(DocUnitList, {
      props: {
        docUnits: [docUnit],
      },
      global: {
        plugins: [vuetify, router],
      },
    })

    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).toBeNull()
  })

  test("delete emits event", async () => {
    const docUnit = new DocUnit("1", { aktenzeichen: "foo" })

    const { emitted } = render(DocUnitList, {
      props: {
        docUnits: [docUnit],
      },
      global: {
        plugins: [vuetify, router],
      },
    })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    expect(emitted().deleteDocUnit).toBeTruthy()
  })
})
