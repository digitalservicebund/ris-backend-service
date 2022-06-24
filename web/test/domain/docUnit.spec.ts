import { describe, it, expect } from "vitest"
import DocUnit, { CoreData, Texts } from "../../src/domain/docUnit"

describe("DocUnit", () => {
  it("instantiates with empty strings", () => {
    const docUnit = new DocUnit("foo")
    expect(docUnit.aktenzeichen).toEqual(undefined)
    expect(docUnit.uuid).toEqual(undefined)
  })

  it("instantiates with values", () => {
    const docUnit = new DocUnit("foo")
    expect(docUnit.id).toEqual("foo")
    expect(docUnit.uuid).toEqual(undefined)
  })

  it("returns core Data as object", () => {
    const docUnit = new DocUnit("foo")
    docUnit.aktenzeichen = "bar"
    docUnit.gerichtssitz = "baz"

    const coreData: CoreData = docUnit.coreData
    expect(coreData.aktenzeichen).toBe("bar")
    expect(coreData.gerichtssitz).toBe("baz")
  })

  it("returns texts as object", () => {
    const docUnit = new DocUnit("foo")
    docUnit.gruende = "bar"
    docUnit.orientierungssatz = "baz"

    const docUnitTexts: Texts = docUnit.texts
    expect(docUnitTexts.gruende).toBe("bar")
    expect(docUnitTexts.orientierungssatz).toBe("baz")
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
