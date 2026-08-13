export class ApiError extends Error {
  constructor(message, status, path, timestamp) {
    super(message);

    this.name = "ApiError";
    this.status = status;
    this.path = path;
    this.timestamp = timestamp;
  }
}
