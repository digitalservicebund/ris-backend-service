import { describe, it, expect } from "vitest"
import ProceedingDecision from "@/domain/proceedingDecision"

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
      dataSource: "PROCEEDING_DECISION",
    })
    expect(proceedingDecision.isDocUnit()).toBeFalsy()
  })

  it("returns true if linked to other docunit", () => {
    const proceedingDecision = new ProceedingDecision({
      dataSource: "NEURIS",
    })
    expect(proceedingDecision.isDocUnit()).toBeTruthy()
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
      decisionDate: "2019-12-31T23:00:00Z",
    })
    expect(proceedingDecision.missingRequiredFields).toStrictEqual([
      "fileNumber",
    ])
  })

  it("missing date with dateKnown false should be valid", () => {
    const decisionWithoutDateKnown = new ProceedingDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      fileNumber: "bar",
    })
    expect(decisionWithoutDateKnown.missingRequiredFields).toStrictEqual([
      "decisionDate",
    ])

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
