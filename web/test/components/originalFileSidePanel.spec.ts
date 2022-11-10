import { render } from "@testing-library/vue"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

describe("originalFile SidePanel", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw-documentUnit-:documentNumber-files",
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
      global: { plugins: [router] },
    })
    getByText("Originaldokument")
    expect(queryByText("Dokument wird geladen")).not.toBeInTheDocument()
    expect(
      queryByText("Es wurde noch kein Originaldokument hochgeladen.")
    ).not.toBeInTheDocument()
  })

  test("renders loading if no file provided", async () => {
    const { getByText } = render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: true,
      },
      global: { plugins: [router] },
    })
    getByText("Dokument wird geladen")
  })

  test("links to file upload if documentUnit has no file", async () => {
    const { getByText } = render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: false,
      },
      global: { plugins: [router] },
    })
    getByText("Es wurde noch kein Originaldokument hochgeladen.")
    getByText("Zum Upload")
  })
})
