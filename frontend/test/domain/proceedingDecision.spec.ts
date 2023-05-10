import { describe, it, expect } from "vitest"
import { ProceedingDecision } from "@/domain/proceedingDecision"

describe("ProceedingDecision", () => {
  it("instantiates a proceeding decision", () => {
    const proceedingDecision = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      documentType: {
        label: "testDocumentType",
        jurisShortcut: "testDocumentTypeShortcut",
      },
      date: "hi",
      fileNumber: "bar",
    })
    expect(proceedingDecision.court?.label).toEqual("label1")
    expect(proceedingDecision.documentType?.label).toEqual("testDocumentType")
    expect(proceedingDecision.date).toEqual("hi")
    expect(proceedingDecision.fileNumber).toEqual("bar")
  })

  it("returns false if not linked to other docunit", () => {
    const proceedingDecision = new ProceedingDecision({
      dataSource: "PROCEEDING_DECISION",
    })
    expect(proceedingDecision.hasLink).toBeFalsy()
  })

  it("returns true if linked to other docunit", () => {
    const proceedingDecision = new ProceedingDecision({
      dataSource: "NEURIS",
    })
    expect(proceedingDecision.hasLink).toBeTruthy()
  })

  it("returns a string representation of a proceeding decision", () => {
    const proceedingDecision = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      fileNumber: "bar",
    })
    expect(proceedingDecision.renderDecision).toStrictEqual("label1, bar")
  })

  it("with all required fields missing", () => {
    const proceedingDecision = new ProceedingDecision({})
    expect(proceedingDecision.missingRequiredFields).toStrictEqual([
      "fileNumber",
      "court",
      "date",
    ])
  })

  it("with one required fields missing", () => {
    const proceedingDecision = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      fileNumber: "bar",
    })
    expect(proceedingDecision.missingRequiredFields).toStrictEqual(["date"])
  })
})
