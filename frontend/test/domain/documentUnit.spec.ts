import { describe, it, expect } from "vitest"
import DocumentUnit, { CoreData, Texts } from "../../src/domain/documentUnit"

describe("DocumentUnit", () => {
  it("instantiates with uuid", () => {
    const documentUnit = new DocumentUnit("foo")
    expect(documentUnit.uuid).toEqual("foo")
    expect(documentUnit.id).toEqual(undefined)
  })

  it("returns core Data as object", () => {
    const documentUnit = new DocumentUnit("foo")
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

  it("returns texts as object", () => {
    const documentUnit = new DocumentUnit("foo")
    documentUnit.texts.reasons = "bar"
    documentUnit.texts.headnote = "baz"

    const documentUnitTexts: Texts = documentUnit.texts
    expect(documentUnitTexts.reasons).toBe("bar")
    expect(documentUnitTexts.headnote).toBe("baz")
  })

  it("returns false if no file is attached", () => {
    const documentUnit = new DocumentUnit("foo")
    expect(documentUnit.hasFile).toBeFalsy()

    const documentUnit2 = new DocumentUnit("foo", { s3path: "" })
    expect(documentUnit2.hasFile).toBeFalsy()
  })

  it("returns true if file is attached", () => {
    const documentUnit = new DocumentUnit("foo", { s3path: "foo-path" })
    expect(documentUnit.hasFile).toBeTruthy()
  })

  it("returns all missing required fields", () => {
    const documentUnit = new DocumentUnit("foo", { s3path: "foo-path" })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "category",
    ])
  })

  //TODO naming
  it("returns all missing required fields but one", () => {
    const documentUnit = new DocumentUnit("foo", {
      s3path: "foo-path",
      coreData: { legalEffect: "foo" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "category",
    ])
  })

  //TODO naming
  it("returns all missing required with empty array", () => {
    const documentUnit = new DocumentUnit("foo", {
      s3path: "foo-path",
      coreData: { fileNumbers: [] },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "category",
    ])
  })

  //TODO naming
  it("returns all missing required fields with empty string", () => {
    const documentUnit = new DocumentUnit("foo", {
      s3path: "foo-path",
      coreData: { decisionDate: "" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "legalEffect",
      "category",
    ])
  })

  //TODO naming
  it("returns all missing required fields", () => {
    const documentUnit = new DocumentUnit("foo", {
      s3path: "foo-path",
      coreData: { legalEffect: "foo" },
    })
    expect(documentUnit.missingRequiredFields).toStrictEqual([
      "fileNumbers",
      "court",
      "decisionDate",
      "category",
    ])
  })

  it("returns true when field is required", () => {
    expect(DocumentUnit.isRequiredField("fileNumbers")).toBeTruthy()
  })

  it("returns true when field is required", () => {
    expect(DocumentUnit.isRequiredField("ECLI")).toBeFalsy()
  })
})
