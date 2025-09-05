export type User = {
  id?: string
  name: string
  documentationOffice?: {
    abbreviation: string
  }
  email?: string
  internal?: boolean
  initials: string
}
