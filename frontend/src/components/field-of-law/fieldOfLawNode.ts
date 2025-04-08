import { FieldOfLaw } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import StringsUtil from "@/utils/stringsUtil"

export interface NodeHelperInterface {
  nodes: Map<string, FieldOfLaw>

  getChildren(node: FieldOfLaw): Promise<FieldOfLaw[]>

  getFilteredChildren(
    node: FieldOfLaw,
    expandedNodes: FieldOfLaw[],
  ): Promise<FieldOfLaw[]>

  getAncestors(clickedIdentifier: string): Promise<FieldOfLaw[]>

  reset(): void
}

export class NodeHelper implements NodeHelperInterface {
  nodes = new Map<string, FieldOfLaw>()

  async getAncestors(clickedIdentifier: string): Promise<FieldOfLaw[]> {
    const itemsToReturn = new Map<string, FieldOfLaw>()

    if (StringsUtil.isEmpty(clickedIdentifier)) {
      return Array.from(itemsToReturn.values())
    }
    const response =
      await FieldOfLawService.getTreeForIdentifier(clickedIdentifier)
    if (response.data) {
      this.extractNodes(itemsToReturn, response.data)
    }
    return Array.from(itemsToReturn.values())
  }

  extractNodes(
    itemsToReturn: Map<string, FieldOfLaw>,
    node: FieldOfLaw,
  ): Map<string, FieldOfLaw> {
    itemsToReturn.set(node.identifier, node)
    if (node.children.length > 0) {
      for (const child of node.children) {
        this.extractNodes(itemsToReturn, child)
      }
    }

    return itemsToReturn
  }

  private getChildrenByParentId(parentId: string): FieldOfLaw[] | undefined {
    return this.nodes.get(parentId)?.children
  }

  async getChildren(node: FieldOfLaw): Promise<FieldOfLaw[]> {
    if (node.hasChildren) {
      try {
        const fromLocal = this.getChildrenByParentId(node.identifier)
        if (fromLocal) {
          return fromLocal
        }
        const response = await FieldOfLawService.getChildrenOf(node.identifier)
        if (response.data) {
          return response.data
        } else {
          return []
        }
      } catch {
        return []
      }
    } else {
      return []
    }
  }

  async getFilteredChildren(
    node: FieldOfLaw,
    expandedNodes: FieldOfLaw[],
  ): Promise<FieldOfLaw[]> {
    let children = await this.getChildren(node)
    const expandedNodesIds = expandedNodes.map((node) => node.identifier)
    if (expandedNodesIds.length > 0) {
      children = children.filter((child) =>
        expandedNodesIds.includes(child.identifier),
      )
    }
    return children
  }

  reset(): void {
    this.nodes.clear()
  }
}
