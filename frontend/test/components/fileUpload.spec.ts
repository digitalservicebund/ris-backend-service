import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { describe, test } from "vitest"
import FileUpload from "../../src/components/FileUpload.vue"

describe("FileUpload", () => {
  test("renders file upload with default props", async () => {
    render(FileUpload, {
      props: {
        error: undefined,
      },
    })

    screen.getByText("Datei in diesen Bereich ziehen")
  })

  test("shows upload error", async () => {
    render(FileUpload, {
      props: {
        error: { title: "test", description: "this is an error" },
      },
    })
    screen.getByText("this is an error")
  })

  test("fires events on file selected", async () => {
    const { emitted } = render(FileUpload)
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
    expect(emitted().fileSelected).toBeTruthy()
  })

  test("shows spinner when set on loading", async () => {
    render(FileUpload, {
      props: {
        isLoading: true,
      },
    })

    screen.getByText("Upload läuft")
  })
})
