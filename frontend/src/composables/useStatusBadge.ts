import { computed } from "vue"
import DocumentUnit from "@/domain/documentUnit"

export function useStatusBadge(status: DocumentUnit["status"]) {
  return computed(() => {
    if (status == "PUBLISHED") {
      return {
        label: "status",
        value: "veröffentlicht",
        icon: "campaign",
        color: "black",
      }
    }
    if (status == "UNPUBLISHED") {
      return {
        label: "status",
        value: "unveröffentlicht",
        icon: "disabled_visible",
        color: "black",
      }
    }
    return undefined
  })
}
