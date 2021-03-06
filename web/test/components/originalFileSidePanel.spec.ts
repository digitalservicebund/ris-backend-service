import { render } from "@testing-library/vue"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

describe("originalFile SidePanel", () => {
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

  test("panel not visible if closed", () => {
    const { queryByText, getByText } = render(OriginalFileSidePanel, {
      props: {
        open: false,
        hasFile: true,
        file: "<p>Foo</p>",
      },
      global: { plugins: [vuetify, router] },
    })
    getByText("Originaldokument")
    expect(queryByText("Dokument wird geladen")).toBeNull()
    expect(
      queryByText("Es wurde noch kein Originaldokument hochgeladen.")
    ).toBeNull()
  })

  test("renders loading if no file provided", async () => {
    const { getByText } = render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: true,
      },
      global: { plugins: [vuetify, router] },
    })
    getByText("Dokument wird geladen")
  })

  test("links to file upload if docUnit has no file", async () => {
    const { getByText } = render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: false,
      },
      global: { plugins: [vuetify, router] },
    })
    getByText("Es wurde noch kein Originaldokument hochgeladen.")
    getByText("Zum Upload")
  })
})
