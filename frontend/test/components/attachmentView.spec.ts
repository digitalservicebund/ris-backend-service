import { render, screen } from "@testing-library/vue"
import AttachmentView from "@/components/AttachmentView.vue"
import fileService from "@/services/fileService"

describe("attachments are shown in side panel", () => {
  test("panel shows content", () => {
    vi.spyOn(fileService, "getAttachmentAsHtml").mockImplementation(() =>
      Promise.resolve({ status: 200, data: { html: "content" } }),
    )

    render(AttachmentView, {
      props: {
        documentUnitUuid: "123",
        s3Path: "foo-path",
      },
    })
    expect(screen.getByTestId("text-editor")).toBeInTheDocument()
  })
})
