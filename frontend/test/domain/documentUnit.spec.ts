import { Attachment } from "@/domain/attachment"
import { CoreData } from "@/domain/coreData"
import { Decision, LongTexts, ShortTexts } from "@/domain/decision"

describe("DocumentUnit", () => {
  it("instantiates with uuid", () => {
    const documentUnit = new Decision("foo")
    expect(documentUnit.uuid).toEqual("foo")
    expect(documentUnit.id).toEqual(undefined)
  })

  it("returns core Data as object", () => {
    const documentUnit = new Decision("foo")
    documentUnit.coreData.fileNumbers = ["bar"]
    documentUnit.coreData.court = {
      type: "baz",
      location: "baz",
      label: "baz",
    }

    const coreData: CoreData = documentUnit.coreData
    expect(coreData.fileNumbers).toStrictEqual(["bar"])
    expect(coreData.court?.location).toBe("baz")
  })

  it("returns short texts as object", () => {
    const documentUnit = new Decision("foo")
    documentUnit.shortTexts.headnote = "baz"

    const documentUnitTexts: ShortTexts = documentUnit.shortTexts
    expect(documentUnitTexts.headnote).toBe("baz")
  })

  it("returns long texts as object", () => {
    const documentUnit = new Decision("foo")
    documentUnit.longTexts.reasons = "bar"

    const documentUnitTexts: LongTexts = documentUnit.longTexts
    expect(documentUnitTexts.reasons).toBe("bar")
  })

  it("returns false if no file is attached", () => {
    const documentUnit = new Decision("foo")
    expect(documentUnit.hasAttachments).toBeFalsy()

    const documentUnit2 = new Decision("foo", { attachments: [] })
    expect(documentUnit2.hasAttachments).toBeFalsy()
  })

  it("returns true if file is attached", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
    })
    expect(documentUnit.hasAttachments).toBeTruthy()
  })

  it("returns all missing required fields", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "documentType",
    ])
  })

  it("identify missing fields correctly if one field is set", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
      coreData: { legalEffect: "foo" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "documentType",
    ])
  })

  it("indentify empty fileNumbers as missing", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
      coreData: { fileNumbers: [] },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "documentType",
    ])
  })

  it("indentify empty decisionDate as missing", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
      coreData: { decisionDate: "" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "documentType",
    ])
  })

  it("indentify all missing required fields", () => {
    const documentUnit = new Decision("foo", {
      attachments: [mockAttachment],
      coreData: { legalEffect: "foo" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "documentType",
    ])
  })
})

const mockAttachment: Attachment = {
  id: "123",
  uploadTimestamp: "2024-06-01T12:00:00Z",
  name: "name",
  format: "docx",
  s3path: "foo-path",
}
