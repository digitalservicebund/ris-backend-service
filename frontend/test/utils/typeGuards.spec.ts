import { DocumentUnit } from "@/domain/documentUnit"
import PendingProceeding from "@/domain/pendingProceeding"
import { isDocumentUnit, isPendingProceeding } from "@/utils/typeGuards"

describe("isDocumentUnit", () => {
  it("returns true for a DocumentUnit", () => {
    const doc = new DocumentUnit("test-id-1", {
      documentNumber: "123",
      shortTexts: {},
    })
    expect(isDocumentUnit(doc)).toBe(true)
  })

  it("returns false for a PendingProceeding", () => {
    const doc = new PendingProceeding("test-id-2", {
      documentNumber: "456",
      shortTexts: {},
      coreData: {},
    })
    expect(isDocumentUnit(doc)).toBe(false)
  })

  it("returns false for undefined", () => {
    expect(isDocumentUnit(undefined)).toBe(false)
  })

  it("returns false for null", () => {
    expect(isDocumentUnit(null)).toBe(false)
  })
})

describe("isPendingProceeding", () => {
  it("returns true for a PendingProceeding", () => {
    const doc = new PendingProceeding("test-id-3", {
      documentNumber: "789",
      shortTexts: {},
      coreData: {},
    })
    expect(isPendingProceeding(doc)).toBe(true)
  })

  it("returns false for a DocumentUnit", () => {
    const doc = new DocumentUnit("test-id-4", {
      documentNumber: "101",
      shortTexts: {},
    })
    expect(isPendingProceeding(doc)).toBe(false)
  })

  it("returns false for undefined", () => {
    expect(isPendingProceeding(undefined)).toBe(false)
  })

  it("returns false for null", () => {
    expect(isPendingProceeding(null)).toBe(false)
  })
})
