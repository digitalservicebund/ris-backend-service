import { fireEvent, render, waitFor } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileUpload from "../../src/components/FileUpload.vue"
import DocumentUnit from "@/domain/documentUnit"
import fileService from "@/services/fileService"

describe("FileUpload", () => {
  vi.mock("@/services/fileService")

  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-documentUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders file upload with default props", async () => {
    const { getByText } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    getByText(
      "Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des Originaldokumentes aus"
    )
  })

  test("upload docx file", async () => {
    vi.spyOn(fileService, "upload").mockImplementation(() =>
      Promise.resolve({
        status: 201,
        data: new DocumentUnit("1"),
      })
    )

    const { getByText, getByLabelText, emitted } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const inputEl = getByLabelText("Festplatte durchsuchen", {
      selector: "input",
      exact: false,
    })

    const file = new File(["test"], "sample.docx", {
      type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() => fireEvent.update(inputEl))

    await flushPromises()

    getByText("Die Datei sample.docx wurde erfolgreich hochgeladen", {
      exact: false,
    })
    expect(emitted().updateDocumentUnit).toBeTruthy()
  })

  test("upload fails if file has no docx format", async () => {
    const { getByText, getByLabelText, emitted } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const inputEl = getByLabelText("Upload File")

    const file = new File(["test"], "sample.png", {
      type: "image/png",
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() => fireEvent.update(inputEl))

    expect(emitted().updateDocumentUnit).not.toBeTruthy()
    getByText("Datei in diesen Bereich ziehen", { exact: false })
  })

  test("upload fails because the file is too large", async () => {
    vi.spyOn(fileService, "upload").mockImplementation(() =>
      Promise.resolve({
        status: 413,
        error: {
          title: "Die Datei darf max. 20 MB groß sein.",
          description: "Bitte laden Sie eine kleinere Datei hoch.",
        },
      })
    )

    const { getByText, getByLabelText } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const file = new File(["test"], "sample.docx")

    const inputEl = getByLabelText("Festplatte durchsuchen", {
      selector: "input",
      exact: false,
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() => fireEvent.update(inputEl))

    await flushPromises()

    getByText("Die Datei darf max. 20 MB groß sein.", { exact: false })
    getByText("Bitte laden Sie eine kleinere Datei hoch.", { exact: false })
  })

  test.skip("upload fails due to unknown error while sending via service", async () => {
    vi.spyOn(fileService, "upload").mockImplementation(() =>
      Promise.resolve({
        status: 500,
        error: {
          title: "Leider ist ein Fehler aufgetreten.",
          description:
            "Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.",
        },
      })
    )

    const { getByText, getByLabelText } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    const file = new File(["test"], "sample.docx")

    const inputEl = getByLabelText("Festplatte durchsuchen", {
      selector: "input",
      exact: false,
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await waitFor(() => fireEvent.update(inputEl))

    await flushPromises()

    getByText("Leider ist ein Fehler aufgetreten.", { exact: false })
    getByText("Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.", {
      exact: false,
    })
  })
})
