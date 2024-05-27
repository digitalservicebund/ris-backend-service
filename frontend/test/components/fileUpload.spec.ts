import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import FileUpload from "@/components/FileUpload.vue"

describe("FileUpload", () => {
  // TODO: more test needed because of new props
  test("renders file upload with default props", async () => {
    render(FileUpload, {
      props: {},
    })

    screen.getByText("Ziehen Sie Ihre Dateien in diesem Bereich.")
  })

  test.each([
    [
      [
        new File(["test"], "sample.docx", {
          type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        }),
      ],
    ],

    [
      [
        new File(["testA"], "sample-a.docx", {
          type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        }),
        new File(["testB"], "sample-b.docx", {
          type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        }),
      ],
    ],
  ])(`fires events on files selected`, async (files) => {
    const { emitted } = render(FileUpload)
    const inputEl = screen.getByLabelText("Oder hier auswählen", {
      selector: "input",
      exact: false,
    })

    Object.defineProperty(inputEl, "files", {
      value: files,
      configurable: true,
    })

    await fireEvent.update(inputEl)
    await flushPromises()
    expect(emitted().filesSelected).toBeTruthy()
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
