import { computed } from "vue"
import DocumentUnit from "@/domain/documentUnit"

export function useStatusBadge(status: DocumentUnit["status"]) {
  const badge = {
    label: "status",
    value: "veröffentlicht",
    icon: "campaign",
    color: "black",
  }

  return computed(() => {
    if (status?.status == "PUBLISHED") {
      if (status?.withError) {
        badge.value = "veröffentlicht mit Fehlern"
        badge.icon = "error_outline"
      } else {
        badge.value = "veröffentlicht"
        badge.icon = "campaign"
      }
    }
    if (status?.status == "UNPUBLISHED") {
      if (status?.withError) {
        badge.value = "Nicht veröffentlicht (Fehler)"
        badge.icon = "error_outline"
      } else {
        badge.value = "unveröffentlicht"
        badge.icon = "disabled_visible"
      }
    }
    if (status?.status == "PUBLISHING") {
      if (status?.withError) {
        badge.value = "Fehler beim Veröffentlichen"
        badge.icon = "error_outline"
      } else {
        badge.value = "in Veröffentlichung"
        badge.icon = "access_time"
      }
    }
    return badge
  })
}
