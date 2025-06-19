import { FieldOfLaw } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import StringsUtil from "@/utils/stringsUtil"

export interface NodeHelperInterface {
  nodes: Map<string, FieldOfLaw>

  getChildren(node: FieldOfLaw): Promise<FieldOfLaw[]>

  getAncestors(clickedIdentifier: string): Promise<FieldOfLaw[]>
}

export class NodeHelper implements NodeHelperInterface {
  nodes = new Map<string, FieldOfLaw>()

  async getAncestors(clickedIdentifier: string): Promise<FieldOfLaw[]> {
    const itemsToReturn = new Map<string, FieldOfLaw>()

    if (StringsUtil.isEmpty(clickedIdentifier)) {
      return Array.from(itemsToReturn.values())
    }
    const response =
      await FieldOfLawService.getParentAndChildrenForIdentifier(
        clickedIdentifier,
      )
    if (response.data) {
      this.extractChildren(itemsToReturn, response.data)
      this.extractParents(itemsToReturn, response.data)
    }
    return Array.from(itemsToReturn.values())
  }

  extractChildren(
    itemsToReturn: Map<string, FieldOfLaw>,
    node: FieldOfLaw,
  ): Map<string, FieldOfLaw> {
    itemsToReturn.set(node.identifier, node)
    if (node.children.length > 0) {
      for (const child of node.children) {
        this.extractChildren(itemsToReturn, child)
      }
    }

    return itemsToReturn
  }

  extractParents(
    itemsToReturn: Map<string, FieldOfLaw>,
    node: FieldOfLaw,
  ): Map<string, FieldOfLaw> {
    if (node.parent) {
      itemsToReturn.set(node.parent.identifier, node.parent)
      if (node.parent.parent) {
        this.extractParents(itemsToReturn, node.parent)
      }
    }

    return itemsToReturn
  }

  private getChildrenByParentId(parentId: string): FieldOfLaw[] | undefined {
    const parentNode = this.nodes.get(parentId)
    return parentNode?.hasChildren && parentNode.children.length > 0
      ? parentNode?.children
      : undefined
  }

  async getChildren(node: FieldOfLaw): Promise<FieldOfLaw[]> {
    if (node.hasChildren) {
      const fromLocal = this.getChildrenByParentId(node.identifier)
      if (fromLocal) {
        return fromLocal
      }
      const response = await FieldOfLawService.getChildrenOf(node.identifier)
      if (response.data) {
        // Put resulting elements to nodes map
        response.data.forEach((fieldOfLaw) =>
          this.nodes.set(fieldOfLaw.identifier, fieldOfLaw),
        )
        // Add resulting elements as children to requested node and put it to map
        node.children = response.data
        this.nodes.set(node.identifier, node)
        return response.data
      }
    }
    return []
  }
}
