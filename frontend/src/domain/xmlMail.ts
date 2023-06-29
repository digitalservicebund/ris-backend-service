export enum PublicationLogEntryType {
  Xml = "XML",
  Html = "HTML",
}

export default class XmlMail {
  public type?: PublicationLogEntryType
  public date?: string
  public content?: string
  public xml?: string
  public statusMessages?: string[]
  public statusCode?: string
  public receiverAddress?: string
  public mailSubject?: string
  public publishStateDisplayText?: string
}
