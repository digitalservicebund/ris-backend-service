import { describe, it, expect } from "vitest"
import ProceedingDecision from "@/domain/previousDecision"

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
      decisionDate: "hi",
      fileNumber: "bar",
    })
    expect(proceedingDecision.court?.label).toEqual("label1")
    expect(proceedingDecision.documentType?.label).toEqual("testDocumentType")
    expect(proceedingDecision.decisionDate).toEqual("hi")
    expect(proceedingDecision.fileNumber).toEqual("bar")
  })

  it("instantiates with default unkownData", () => {
    const proceedingDecision = new ProceedingDecision()
    expect(proceedingDecision.dateKnown).toBeTruthy()
  })

  it("returns false if not linked to other docunit", () => {
    const proceedingDecision = new ProceedingDecision({
      documentNumber: undefined,
    })
    expect(proceedingDecision.isReadOnly).toBeFalsy()
  })

  it("returns true if linked to other docunit", () => {
    const proceedingDecision = new ProceedingDecision({
      documentNumber: "ABC",
      referenceFound: true,
    })
    expect(proceedingDecision.isReadOnly).toBeTruthy()
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
      "decisionDate",
    ])
  })

  it("with one required fields missing", () => {
    const proceedingDecision = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      decisionDate: "2019-12-31",
    })
    expect(proceedingDecision.missingRequiredFields).toStrictEqual([
      "fileNumber",
    ])
  })

  it("missing date with dateKnown true should be invalid", () => {
    const decisionWithoutDateKnown = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      dateKnown: true,
      fileNumber: "bar",
    })
    expect(decisionWithoutDateKnown.missingRequiredFields).toStrictEqual([
      "decisionDate",
    ])
  })

  it("missing date with dateKnown false should be invalid", () => {
    const decisionWithDateKnown = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      fileNumber: "bar",
      dateKnown: false,
    })
    expect(decisionWithDateKnown.missingRequiredFields).toStrictEqual([])
  })
})
