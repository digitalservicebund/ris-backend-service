export default class File {
  uploadedDate?: string
  name?: string
  format?: string
  path?: string

  constructor(data: Partial<File> = {}) {
    Object.assign(this, data)
  }
}
