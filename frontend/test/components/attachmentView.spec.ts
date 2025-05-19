import { render, screen } from "@testing-library/vue"
import AttachmentView from "@/components/AttachmentView.vue"
import attachmentService from "@/services/attachmentService"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

describe("attachments are shown in side panel", () => {
  test("panel shows content", async () => {
    const content = "content"
    vi.spyOn(attachmentService, "getAttachmentAsHtml").mockImplementation(() =>
      Promise.resolve({ status: 200, data: { html: content } }),
    )

    useFeatureToggleServiceMock()

    render(AttachmentView, {
      props: {
        documentUnitUuid: "123",
        s3Path: "foo-path",
        format: "docx",
      },
    })

    expect(await screen.findByTestId("text-editor")).toBeInTheDocument()
    expect(await screen.findByText(content)).toBeVisible()
  })
})
