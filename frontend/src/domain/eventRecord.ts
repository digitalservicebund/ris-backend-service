export enum EventRecordType {
  HANDOVER = "PUBLICATION",
  HANDOVER_REPORT = "PUBLICATION_REPORT",
  MIGRATION = "MIGRATION",
}

export default class EventRecord {
  public type?: EventRecordType
  public date?: string
  public content?: string
  public xml?: string
  public statusMessages?: string[]
  public success?: boolean
  public receiverAddress?: string
  public mailSubject?: string
}
