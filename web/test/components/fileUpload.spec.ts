import { fireEvent, render, waitFor } from "@testing-library/vue"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileUpload from "../../src/components/FileUpload.vue"

describe("FileUpload", () => {
  vi.mock("@/services/fileService")
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-docUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders file upload with default props", async () => {
    const { getByText } = render(FileUpload, {
      props: {
        docUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    getByText(
      "Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des Originaldokumentes aus"
    )
  })

  test("upload docx file", async () => {
    const { getByText, getByLabelText, emitted } = render(FileUpload, {
      props: {
        docUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const inputEl = getByLabelText("file-upload")

    const file = new File(["test"], "sample.docx", {
      type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() =>
      fireEvent.change(inputEl, {
        target: { files: [file] },
      })
    )
    getByText("Die Datei sample.docx wurde erfolgreich hochgeladen", {
      exact: false,
    })
    expect(emitted().updateDocUnit).toBeTruthy()
  })

  test("upload fails if file has no docx format", async () => {
    const { getByText, getByLabelText, emitted } = render(FileUpload, {
      props: {
        docUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const inputEl = getByLabelText("file-upload")

    const file = new File(["test"], "sample.png", {
      type: "image/png",
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() =>
      fireEvent.change(inputEl, {
        target: { files: [file] },
      })
    )

    expect(emitted().updateDocUnit).not.toBeTruthy()
    getByText("Datei in diesen Bereich ziehen", { exact: false })
  })
})
