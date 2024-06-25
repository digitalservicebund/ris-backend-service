export type PublicationStatus = {
  publicationStatus?: PublicationState
  withError?: boolean
}
export enum PublicationState {
  PUBLISHED = "PUBLISHED",
  UNPUBLISHED = "UNPUBLISHED",
  PUBLISHING = "PUBLISHING",
  DUPLICATED = "DUPLICATED",
  LOCKED = "LOCKED",
  DELETING = "DELETING",
}

export const Label: Record<PublicationState, string> = {
  [PublicationState.PUBLISHED]: "Veröffentlicht",
  [PublicationState.UNPUBLISHED]: "Unveröffentlicht", // or "Nicht veröffentlicht" when withError
  [PublicationState.PUBLISHING]: "In Veröffentlichung",
  [PublicationState.DUPLICATED]: "Dublette",
  [PublicationState.LOCKED]: "Gesperrt",
  [PublicationState.DELETING]: "Löschen",
}
