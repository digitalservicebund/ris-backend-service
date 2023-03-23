import { ProceedingDecision } from "@/domain/documentUnit"
import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/proceedingDecisionService"

const testModel = {
  court: undefined,
  documentType: undefined,
  date: undefined,
  fileNumber: undefined,
}

describe("documentUnitService", () => {
  vi.mock("@/services/proceedingDecisionService", () => {
    const testGetResponse: ServiceResponse<ProceedingDecision[]> = {
      status: 200,
      data: [
        {
          court: undefined,
          documentType: undefined,
          date: undefined,
          fileNumber: undefined,
        },
      ],
    }
    const testPutResponse: ServiceResponse<ProceedingDecision[]> = {
      status: 200,
      data: [
        {
          court: undefined,
          documentType: undefined,
          date: undefined,
          fileNumber: undefined,
        },
      ],
    }
    // const testDeleteResponse: ServiceResponse<ProceedingDecision[]> = {
    //   status: 200,
    //   data: [],
    // }
    return {
      default: {
        getProceedingDecisions: vi.fn().mockReturnValue(testGetResponse),
        addProceedingDecision: vi.fn().mockReturnValue(testPutResponse),
        // deleteProceedingDecision: vi.fn().mockReturnValue(testDeleteResponse),
      },
    }
  })

  it("get proceeding decisions", async () => {
    const result = await service.getProceedingDecisions("123")
    expect(result.data).toEqual([testModel])
  })

  it("add proceeding decision", async () => {
    const result = await service.addProceedingDecision("123", testModel)
    expect(result.data).toEqual([testModel])
  })

  // it("delete proceeding decision", async () => {
  //   const result = await service.deleteProceedingDecisions("123", testModel)
  //   expect(result.data).toEqual([])
  // })
})
