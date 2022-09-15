import { describe, it, expect } from "vitest"
import DocUnit, { CoreData, Texts } from "../../src/domain/docUnit"

describe("DocUnit", () => {
  it("instantiates with uuid", () => {
    const docUnit = new DocUnit("foo")
    expect(docUnit.uuid).toEqual("foo")
    expect(docUnit.id).toEqual(undefined)
  })

  it("returns core Data as object", () => {
    const docUnit = new DocUnit("foo")
    docUnit.fileNumber = "bar"
    docUnit.courtLocation = "baz"

    const coreData: CoreData = docUnit.coreData
    expect(coreData.fileNumber).toBe("bar")
    expect(coreData.courtLocation).toBe("baz")
  })

  it("returns texts as object", () => {
    const docUnit = new DocUnit("foo")
    docUnit.reasons = "bar"
    docUnit.headnote = "baz"

    const docUnitTexts: Texts = docUnit.texts
    expect(docUnitTexts.reasons).toBe("bar")
    expect(docUnitTexts.headnote).toBe("baz")
  })

  it("returns false if no file is attached", () => {
    const docUnit = new DocUnit("foo")
    expect(docUnit.hasFile).toBeFalsy()

    const docUnit2 = new DocUnit("foo", { s3path: "" })
    expect(docUnit2.hasFile).toBeFalsy()
  })

  it("returns true if file is attached", () => {
    const docUnit = new DocUnit("foo", { s3path: "foo-path" })
    expect(docUnit.hasFile).toBeTruthy()
  })
})
