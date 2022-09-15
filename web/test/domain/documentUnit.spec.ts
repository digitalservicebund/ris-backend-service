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
    documentUnit.fileNumber = "bar"
    documentUnit.courtLocation = "baz"

    const coreData: CoreData = documentUnit.coreData
    expect(coreData.fileNumber).toBe("bar")
    expect(coreData.courtLocation).toBe("baz")
  })

  it("returns texts as object", () => {
    const documentUnit = new DocumentUnit("foo")
    documentUnit.reasons = "bar"
    documentUnit.headnote = "baz"

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
})
