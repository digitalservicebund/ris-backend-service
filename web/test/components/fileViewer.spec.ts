import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import FileViewer from "../../src/components/FileViewer.vue"

const S3PATH = "47a77c3f-a05d-4362-97ea-d54a35236a67"
const FILE_NAME = "some-formatting.docx"
const FILE_TYPE = "docx"
const USER_NAME = "USER NAME"
const LABEL_TEXTES = ["Hochgeladen am", "Format", "Von", "Dateiname"]
const DELETE_BTN_TEXT = "Datei löschen"
const DEFAULT_EDITOR_TEXT = "Loading data ...."
describe("file viewer", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  vi.mock("@/services/fileService", () => {
    return {
      default: {
        getDocxFileAsHtml: vi.fn().mockReturnValue("<p>foo</p>"),
      },
    }
  })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-documentUnit-:documentNumber-files",
        component: {},
      },
    ],
  })

  test("file viewer should be rendered", async () => {
    const { getByText, container } = render(FileViewer, {
      props: {
        s3Path: S3PATH,
        fileName: FILE_NAME,
        fileType: FILE_TYPE,
        uploadTimeStamp: getUploadTimeStampToUpload(),
      },
      global: { plugins: [router] },
    })

    LABEL_TEXTES.forEach((labelText) => getByText(labelText))
    getByText(FILE_TYPE)
    getByText(FILE_NAME)
    getByText(USER_NAME)
    getByText(DELETE_BTN_TEXT)
    getByText(getUploadTimeStampToCheck())
    // To check if default value of textEditor in page shoud'nt be found.
    // Because after mounted, the value will be replaced by docx content.
    expect(container.textContent?.includes(DEFAULT_EDITOR_TEXT)).toBeFalsy()
  })

  test("file viewer emitted delete uploadfile event", async () => {
    const { emitted } = render(FileViewer, {
      props: {
        s3Path: S3PATH,
        fileName: FILE_NAME,
        fileType: FILE_TYPE,
        uploadTimeStamp: getUploadTimeStampToUpload(),
      },
      global: { plugins: [router] },
    })

    await fireEvent.click(screen.getByText(DELETE_BTN_TEXT, { exact: false }))
    const confirmButton = screen.getByText("Löschen")
    expect(confirmButton).toBeInTheDocument()
    await fireEvent.click(confirmButton)
    expect(emitted().deleteFile).toBeTruthy()
  })
})

// @return "19.07.2019"
function getUploadTimeStampToCheck(): string {
  const today = new Date()
  return `${("0" + today.getDate()).slice(-2)}.${("0" + today.getMonth()).slice(
    -2
  )}.${today.getFullYear()}`
}

// @return "2019-07-19"
function getUploadTimeStampToUpload(): string {
  const today = new Date()
  return `${today.getFullYear()}-${("0" + today.getMonth()).slice(
    -2
  )}-${today.getDate()}`
}
