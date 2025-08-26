<script lang="ts" setup>
import IconBadge from "@/components/IconBadge.vue"
import {
  PortalPublicationStatus,
  PortalPublicationStatusLabel,
} from "@/domain/portalPublicationStatus"

const { status } = defineProps<{ status: PortalPublicationStatus }>()

const StatusColor: Record<PortalPublicationStatus, string> = {
  [PortalPublicationStatus.PUBLISHED]: "bg-green-300",
  [PortalPublicationStatus.UNPUBLISHED]: "bg-blue-300",
  [PortalPublicationStatus.WITHDRAWN]: "bg-orange-300",
}
</script>

<template>
  <IconBadge
    data-testid="portal-publication-status-badge"
    :background-color="StatusColor[status] ?? 'bg-white'"
    class="ml-4 inline-block"
    :label="PortalPublicationStatusLabel[status] ?? 'Unbekannter Status'"
  />
</template>
