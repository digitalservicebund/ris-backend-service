import { computed, FunctionalComponent, SVGAttributes } from "vue"
import DocumentUnit, { PublicationState } from "@/domain/documentUnit"

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
    if (!status) return badge

    switch (status.publicationStatus) {
      case PublicationState.PUBLISHED:
        badge.label = "Veröffentlicht"
        badge.backgroundColor = "bg-green-300"
        break
      case PublicationState.UNPUBLISHED:
        badge.label = status.withError
          ? "Nicht veröffentlicht"
          : "Unveröffentlicht"
        badge.backgroundColor = "bg-blue-300"
        break
      case PublicationState.PUBLISHING:
        badge.label = "In Veröffentlichung"
        badge.backgroundColor = "bg-orange-300"
        break
      case PublicationState.DUPLICATED:
        badge.label = "Dublette"
        badge.backgroundColor = "bg-red-300"
        break
      case PublicationState.LOCKED:
        badge.label = "Gesperrt"
        badge.backgroundColor = "bg-red-300"
        break
      case PublicationState.DELETING:
        badge.label = "Löschen"
        badge.backgroundColor = "bg-red-300"
        break
    }
    return badge
  })
}
