import { DocumentUnit } from "@/domain/documentUnit"
import PreviousDecision from "@/domain/previousDecision"
import errorMessages from "@/i18n/errors.json"
import httpClient from "@/services/httpClient"
import service from "@/services/proceedingDecisionService"

const testProceedingDecision = new PreviousDecision({
  ...{
    court: undefined,
    documentType: undefined,
    date: undefined,
    fileNumber: undefined,
    dateKnown: true,
  },
})

vi.mock("@/services/httpClient")

describe("proceedingDecisionService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("create proceeding decision with success", async () => {
    const httpClientGet = vi.mocked(httpClient).put.mockResolvedValueOnce({
      status: 200,
      data: [testProceedingDecision],
    })

    const result = await service.createProceedingDecision(
      "123",
      testProceedingDecision,
    )
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.data).toEqual([testProceedingDecision])
  })

  it("create proceeding decision with error", async () => {
    const error = {
      title: errorMessages.PROCEEDING_DECISION_COULD_NOT_BE_ADDED.title.replace(
        "${uuid}",
        "123",
      ),
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .put.mockResolvedValueOnce({ status: 500, error: error })

    const result = await service.createProceedingDecision(
      "123",
      testProceedingDecision,
    )
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.error).toEqual(error)
  })

  it("link proceeding decision with success", async () => {
    const parentUuid = "123"
    const childUuid = "456"
    const previousDecision = new PreviousDecision({
      court: {
        type: "testCourtType",
        location: "testCourtLocation",
        label: "label1",
      },
      fileNumber: "bar",
    })
    const documentUnit = new DocumentUnit("id", {
      previousDecisions: [previousDecision],
    })

    const httpClientGet = vi
      .mocked(httpClient)
      .put.mockResolvedValueOnce({ status: 200, data: documentUnit })

    const result = await service.linkProceedingDecision(parentUuid, childUuid)
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.data).toEqual([previousDecision])
  })

  it("link proceeding decision with error", async () => {
    const parentUuid = "123"
    const childUuid = "456"
    const error = {
      title:
        errorMessages.DOCUMENT_UNIT_PROCEEDING_DECISION_COULD_NOT_BE_ADDED.title
          .replace("${childUuid}", childUuid)
          .replace("${parentUuid}", parentUuid),
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .put.mockResolvedValueOnce({ status: 500, error: error })

    const result = await service.linkProceedingDecision(parentUuid, childUuid)
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.error).toEqual(error)
  })

  it("remove proceeding decision with error", async () => {
    const parentUuid = "123"
    const childUuid = "456"
    const error = {
      title:
        errorMessages.DOCUMENT_UNIT_PROCEEDING_DECISION_COULD_NOT_BE_DELETED.title
          .replace("${childUuid}", childUuid)
          .replace("${parentUuid}", parentUuid),
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .delete.mockResolvedValueOnce({ status: 500, error: error })

    const result = await service.removeProceedingDecision(parentUuid, childUuid)
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.error).toEqual(error)
  })
})
