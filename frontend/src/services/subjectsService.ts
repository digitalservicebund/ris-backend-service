import { ServiceResponse } from "./httpClient"
import { mockSubjectNodes, SubjectNode } from "@/domain/SubjectTree"

interface SubjectsService {
  getAllNodes(): Promise<ServiceResponse<SubjectNode[]>>
}

const service: SubjectsService = {
  async getAllNodes() {
    const response = {
      status: 200,
      data: mockSubjectNodes,
    }
    return response
  },
}

export default service
