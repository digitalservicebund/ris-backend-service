export enum UploadStatus {
  UNKNOWN,
  UPLOADING,
  SUCCESSED,
  FAILED,
  FILE_TO_LARGE,
  WRONG_FILE_FORMAT,
}

export const UploadErrorStatus: UploadStatus[] = [
  UploadStatus.FAILED,
  UploadStatus.FILE_TO_LARGE,
  UploadStatus.WRONG_FILE_FORMAT,
]
