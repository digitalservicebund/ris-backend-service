import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import OriginalDocumentAttachmentView from "@/components/OriginalDocumentAttachmentView.vue"
import attachmentService from "@/services/attachmentService"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

describe("attachments are shown in side panel", () => {
  test("panel shows content", async () => {
    const content = "content"
    const spy = vi
      .spyOn(attachmentService, "getAttachmentAsHtml")
      .mockImplementation(() =>
        Promise.resolve({ status: 200, data: { html: content } }),
      )

    useFeatureToggleServiceMock()

    render(OriginalDocumentAttachmentView, {
      props: {
        documentationUnitId: "123",
        attachmentId: "456",
      },
      global: {
        plugins: [createTestingPinia()],
      },
    })

    expect(spy).toHaveBeenCalledOnce()
    expect(await screen.findByTestId("text-editor")).toBeInTheDocument()
    expect(await screen.findByText(content)).toBeVisible()
  })
})
