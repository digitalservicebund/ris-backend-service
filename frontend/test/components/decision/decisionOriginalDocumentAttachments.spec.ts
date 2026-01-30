import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { nextTick } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionOriginalDocumentAttachments from "@/components/DecisionOriginalDocumentAttachments.vue"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import routes from "~/test-helper/routes"

function renderComponent(attachments?: Attachment[]) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(DecisionOriginalDocumentAttachments, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              docunitStore: {
                documentUnit: new Decision("foo", {
                  documentNumber: "1234567891234",
                  originalDocumentAttachments: attachments ?? [],
                }),
              },
            },
          }),
          [router],
        ],
      },
    }),
  }
}

describe("Document Unit Categories", () => {
  test("renders without attachments", async () => {
    renderComponent()

    expect(screen.getByTestId("title")).toBeVisible()
    expect(screen.queryByTestId("attachment-list")).not.toBeInTheDocument()
    expect(
      screen.queryByTestId("attachment-view-side-panel"),
    ).not.toBeInTheDocument()
  })

  test("renders file list if has attachments", async () => {
    const name = "this-is-a-file-name.docx"
    const format = "docx"
    const attachment: Attachment = {
      id: "123",
      name: name,
      format: format,
      uploadTimestamp: "11.04.2024",
    }
    renderComponent([attachment])

    expect(screen.getByTestId("title")).toBeVisible()
    expect(screen.queryByTestId("attachment-list")).toBeVisible()
    expect(screen.queryByText(name)).toBeVisible()
    expect(screen.queryByText(format)).toBeVisible()
    expect(screen.getByTestId("uploaded-at-cell")).toBeInTheDocument()
  })

  test("opens delete modal when 'Datei löschen' is clicked", async () => {
    const name = "this-is-a-file-name.docx"
    const format = "docx"
    const attachment: Attachment = {
      id: "123",
      name: name,
      format: format,
      uploadTimestamp: "11.04.2024",
    }
    renderComponent([attachment])

    screen.getByLabelText("Datei löschen").click()
    await nextTick()
    expect(screen.getByRole("dialog")).toHaveTextContent(
      "Möchten Sie den Anhang this-is-a-file-name.docx wirklich dauerhaft löschen?",
    )
    screen.getByLabelText("Abbrechen").click()
    await nextTick()
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument()
  })
})
