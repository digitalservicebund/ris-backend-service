import { FunctionalComponent, SVGAttributes, computed } from "vue"
import DocumentUnit from "@/domain/documentUnit"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconInPublishing from "~icons/ic/outline-access-time"
import IconPublished from "~icons/ic/outline-campaign"
import IconUnpublished from "~icons/ic/outline-disabled-visible"

export interface Badge {
  label: string
  value: string
  icon?: FunctionalComponent<SVGAttributes>
  color: string
}

export function useStatusBadge(status: DocumentUnit["status"]) {
  const badge: Badge = {
    label: "status",
    value: "",
    icon: undefined,
    color: "black",
  }

  return computed(() => {
    if (status?.publicationStatus == "PUBLISHED") {
      if (status?.withError) {
        badge.value = "veröffentlicht mit Fehlern"
        badge.icon = IconErrorOutline
      } else {
        badge.value = "veröffentlicht"
        badge.icon = IconPublished
      }
    }
    if (status?.publicationStatus == "UNPUBLISHED") {
      if (status?.withError) {
        badge.value = "Nicht veröffentlicht (Fehler)"
        badge.icon = IconErrorOutline
      } else {
        badge.value = "unveröffentlicht"
        badge.icon = IconUnpublished
      }
    }
    if (status?.publicationStatus == "PUBLISHING") {
      if (status?.withError) {
        badge.value = "Fehler beim Veröffentlichen"
        badge.icon = IconErrorOutline
      } else {
        badge.value = "in Veröffentlichung"
        badge.icon = IconInPublishing
      }
    }
    return badge
  })
}
