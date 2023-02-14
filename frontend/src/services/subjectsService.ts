import httpClient, { ServiceResponse } from "./httpClient"
import mockSubjectNodes from "@/data/mockSubjectNodes.json"
import { SubjectNode } from "@/domain/SubjectTree"

interface SubjectsService {
  // getAllNodes(): Promise<ServiceResponse<SubjectNode[]>>
  getRootNode(): Promise<ServiceResponse<SubjectNode>>
  getChildrenOf(nodeId: string): Promise<ServiceResponse<SubjectNode[]>>
  getTreeForSubjectFieldNumber(
    nodeId: string
  ): Promise<ServiceResponse<SubjectNode>>
}

const service: SubjectsService = {
  /*async getAllNodes() {
    const response = {
      status: 200,
      data: mockSubjectNodes.items,
    }
    return response
  },*/
  async getRootNode() {
    const response = {
      status: 200,
      data: mockSubjectNodes.items.filter((node) => node.id === "root")[0],
    }
    return response
  },
  async getChildrenOf(nodeId: string) {
    // mock edges
    let children: string[] = []
    switch (nodeId) {
      case "root":
        children = ["01-01", "02-01"]
        break
      case "01-01":
        children = ["01-02"]
        break
      case "02-01":
        children = ["ST-02-02", "02-03"]
        break
      case "02-03":
        children = ["03-01"]
        break
    }
    const response = {
      status: 200,
      data: children.map(
        (childId) =>
          mockSubjectNodes.items.filter((node) => node.id === childId)[0]
      ),
    }
    return response
  },
  async getTreeForSubjectFieldNumber(nodeId: string) {
    const response = await httpClient.get<SubjectNode>(
      `caselaw/lookuptable/subjectFields/${nodeId}/tree`
    )
    if (response.data) {
      console.log("service - load tree:", response.data)
    }
    if (response.status >= 300) {
      response.error = {
        title: "Ausgewähltes Sachgebiet konnten nicht geladen werden.",
      }
    }
    return response
  },
}

export default service
