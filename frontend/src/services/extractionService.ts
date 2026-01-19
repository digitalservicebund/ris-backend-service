import httpClient, { ServiceResponse } from "./httpClient"
import { Extraction } from "@/components/extraction/types"

interface ExtractionRequest {
  html: string
  court?: string
}

interface ExtractionResponse {
  extractions: Extraction[]
}

interface ExtractionService {
  getExtractions(
    body: ExtractionRequest,
  ): Promise<ServiceResponse<ExtractionResponse>>
}

const service: ExtractionService = {
  async getExtractions(body) {
    const response = await httpClient.post<
      ExtractionRequest,
      ExtractionResponse
    >(
      `caselaw/extract`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
      body,
    )
    return response
  },
}

export default service
export type { ExtractionRequest, ExtractionResponse }
