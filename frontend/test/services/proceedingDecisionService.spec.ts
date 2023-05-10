import { ProceedingDecision } from "@/domain/proceedingDecision"
import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/proceedingDecisionService"

const testModel = new ProceedingDecision({
  ...{
    court: undefined,
    documentType: undefined,
    date: undefined,
    fileNumber: undefined,
  },
})

describe("documentUnitService", () => {
  vi.mock("@/services/proceedingDecisionService", () => {
    const testPutResponse: ServiceResponse<ProceedingDecision[]> = {
      status: 200,
      data: [
        new ProceedingDecision({
          ...{
            court: undefined,
            documentType: undefined,
            date: undefined,
            fileNumber: undefined,
          },
        }),
      ],
    }
    return {
      default: {
        createProceedingDecision: vi.fn().mockReturnValue(testPutResponse),
      },
    }
  })

  it("add proceeding decision", async () => {
    const result = await service.createProceedingDecision("123", testModel)
    expect(result.data).toEqual([testModel])
  })
})
