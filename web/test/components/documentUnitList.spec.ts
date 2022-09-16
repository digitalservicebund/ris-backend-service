import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("documentUnit list", () => {
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

  test("renders fallback if no documentUnits found", async () => {
    render(DocumentUnitList, {
      props: {
        documentUnits: [],
      },
      global: {
        plugins: [vuetify, router],
      },
    })

    await screen.findByText("Keine Dokumentationseinheiten gefunden")
  })

  test("renders documentUnits", async () => {
    const documentUnit = new DocumentUnit("1", { fileNumber: "foo" })

    render(DocumentUnitList, {
      props: {
        documentUnits: [documentUnit],
      },
      global: {
        plugins: [vuetify, router],
      },
    })

    await screen.findByText("foo")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })

  test("delete emits event", async () => {
    const documentUnit = new DocumentUnit("1", { fileNumber: "foo" })

    const { emitted } = render(DocumentUnitList, {
      props: {
        documentUnits: [documentUnit],
      },
      global: {
        plugins: [vuetify, router],
      },
    })

    await fireEvent.click(
      screen.getByLabelText("Dokumentationseinheit löschen")
    )
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
