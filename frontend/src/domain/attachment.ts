export default class Attachment {
  uploadedDate?: string
  name?: string
  format?: string
  s3path?: string

  constructor(data: Partial<Attachment> = {}) {
    Object.assign(this, data)
  }
}
