export enum UploadStatus {
  UNKNOWN,
  UPLOADING,
  SUCCESSED,
  FAILED,
  FILE_TOO_LARGE,
  WRONG_FILE_FORMAT,
}

export const UploadErrorStatus: UploadStatus[] = [
  UploadStatus.FAILED,
  UploadStatus.FILE_TOO_LARGE,
  UploadStatus.WRONG_FILE_FORMAT,
]
