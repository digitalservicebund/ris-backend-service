import { ServiceResponse } from "./httpClient"
import mockSubjectNodes from "@/data/mockSubjectNodes.json"
import { SubjectNode } from "@/domain/SubjectTree"

interface SubjectsService {
  getAllNodes(): Promise<ServiceResponse<SubjectNode[]>>
}

const service: SubjectsService = {
  async getAllNodes() {
    const response = {
      status: 200,
      data: mockSubjectNodes.items,
    }
    return response
  },
}

export default service
