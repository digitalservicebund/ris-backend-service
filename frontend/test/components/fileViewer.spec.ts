import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import FileViewer from "@/components/FileViewer.vue"

const UUID = "88888888-4444-4444-4444-121212121212"
const FILE_NAME = "some-formatting.docx"
const FILE_TYPE = "docx"
const USER_NAME = "USER NAME"
const LABEL_TEXTES = ["Hochgeladen am", "Format", "Von", "Dateiname"]
const DELETE_BTN_TEXT = "Datei löschen"
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
        name: "caselaw-documentUnit-:documentNumber-files",
        component: {},
      },
    ],
  })

  test("file viewer should be rendered", async () => {
    render(FileViewer, {
      props: {
        uuid: UUID,
        fileName: FILE_NAME,
        fileType: FILE_TYPE,
        uploadTimeStamp: getUploadTimeStampToUpload(),
      },
      global: { plugins: [router] },
    })

    LABEL_TEXTES.forEach((labelText) => screen.getByText(labelText))
    screen.getByText(FILE_TYPE)
    screen.getByText(FILE_NAME)
    screen.getByText(USER_NAME)
    screen.getByText(DELETE_BTN_TEXT)
    screen.getByText(getUploadTimeStampToCheck())
  })

  test("file viewer emitted delete uploadfile event", async () => {
    const { emitted } = render(FileViewer, {
      props: {
        uuid: UUID,
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
  /*const today = new Date()
  return `${("0" + today.getDate()).slice(-2)}.${("0" + today.getMonth()).slice(
    -2
  )}.${today.getFullYear()}`*/
  return "19.07.2019"
}

// @return "2019-07-19"
function getUploadTimeStampToUpload(): string {
  /*const today = new Date()
  return `${today.getFullYear()}-${("0" + today.getMonth()).slice(
    -2
  )}-${today.getDate()}`*/
  return "2019-07-19"
}
