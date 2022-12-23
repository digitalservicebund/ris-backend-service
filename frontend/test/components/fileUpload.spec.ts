import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import FileUpload from "../../src/components/FileUpload.vue"
import DocumentUnit from "@/domain/documentUnit"
import fileService from "@/services/fileService"

describe("FileUpload", () => {
  vi.mock("@/services/fileService")

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw-documentUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders file upload with default props", async () => {
    render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [router] },
    })

    screen.getByText(
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

    const { emitted } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [router] },
    })

    const inputEl = screen.getByLabelText("oder Datei auswählen", {
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

    await fireEvent.update(inputEl)

    await flushPromises()

    expect(emitted().updateDocumentUnit).toBeTruthy()
  })

  test("upload fails if file has no docx format", async () => {
    const { emitted } = render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [router] },
    })

    const inputEl = screen.getByLabelText("Upload File")

    const file = new File(["test"], "sample.png", {
      type: "image/png",
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await fireEvent.update(inputEl)

    expect(emitted().updateDocumentUnit).not.toBeTruthy()
    screen.getByText("Datei in diesen Bereich ziehen", { exact: false })
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

    render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [router] },
    })

    const file = new File(["test"], "sample.docx")

    const inputEl = screen.getByLabelText("oder Datei auswählen", {
      selector: "input",
      exact: false,
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await fireEvent.update(inputEl)

    await flushPromises()

    screen.getByText("Die Datei darf max. 20 MB groß sein.", { exact: false })
    screen.getByText("Bitte laden Sie eine kleinere Datei hoch.", {
      exact: false,
    })
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

    render(FileUpload, {
      props: {
        documentUnitUuid: "1",
      },
      global: { plugins: [router] },
    })

    const file = new File(["test"], "sample.docx")

    const inputEl = screen.getByLabelText("oder Datei auswählen", {
      selector: "input",
      exact: false,
    })

    Object.defineProperty(inputEl, "files", {
      value: [file],
      configurable: true,
    })

    await fireEvent.update(inputEl)

    await flushPromises()

    screen.getByText("Leider ist ein Fehler aufgetreten.", { exact: false })
    screen.getByText(
      "Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.",
      {
        exact: false,
      }
    )
  })
})
