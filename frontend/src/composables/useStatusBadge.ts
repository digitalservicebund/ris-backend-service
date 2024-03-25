import { FunctionalComponent, SVGAttributes, computed } from "vue"
import DocumentUnit from "@/domain/documentUnit"

export interface Badge {
  label: string
  icon?: FunctionalComponent<SVGAttributes>
  color: string
  backgroundColor: string
}

export function useStatusBadge(status: DocumentUnit["status"]) {
  const badge: Badge = {
    label: "",
    icon: undefined,
    color: "black",
    backgroundColor: "white",
  }

  return computed(() => {
    if (status?.publicationStatus == "PUBLISHED") {
      badge.label = "Veröffentlicht"
      badge.backgroundColor = "bg-green-300"
    }
    if (status?.publicationStatus == "UNPUBLISHED") {
      badge.label = status?.withError
        ? "Nicht veröffentlicht"
        : "Unveröffentlicht"
      badge.backgroundColor = "bg-blue-300"
    }
    if (status?.publicationStatus == "PUBLISHING") {
      badge.label = "In Veröffentlichung"
      badge.backgroundColor = "bg-orange-300"
    }

    return badge
  })
}
