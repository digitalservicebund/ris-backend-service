export enum PortalPublicationStatus {
  PUBLISHED = "PUBLISHED",
  UNPUBLISHED = "UNPUBLISHED",
  WITHDRAWN = "WITHDRAWN",
}

export const PortalPublicationStatusLabel: Record<
  PortalPublicationStatus,
  string
> = {
  [PortalPublicationStatus.PUBLISHED]: "Veröffentlicht",
  [PortalPublicationStatus.UNPUBLISHED]: "Unveröffentlicht",
  [PortalPublicationStatus.WITHDRAWN]: "Zurückgezogen",
}
