import service from "@/services/attachmentService"

describe("attachmentService", () => {
  it("should return 415 if not docx file", async () => {
    const testFile = new File([new Blob(["foo"])], "test.pdf")

    const result = await service.upload("123", testFile)
    expect(result.error?.title).toEqual("hat ein falsches Format.")
  })
})
