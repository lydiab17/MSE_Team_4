package com.evote.app.votingmanagement.ui.api.exceptions;

import java.net.URI;

public class VotingApiException extends RuntimeException {
  public VotingApiException(String message) { super(message); }
  public VotingApiException(String message, Throwable cause) { super(message, cause); }
}

class VotingApiTransportException extends VotingApiException {
  public VotingApiTransportException(String message, Throwable cause) { super(message, cause); }
}

class VotingApiSerializationException extends VotingApiException {
  public VotingApiSerializationException(String message, Throwable cause) { super(message, cause); }
}

class VotingApiHttpException extends VotingApiException {
  private final int statusCode;
  private final URI uri;
  private final String responseBody;

  public VotingApiHttpException(int statusCode, URI uri, String responseBody) {
    super("HTTP " + statusCode + " für " + uri + (responseBody == null || responseBody.isBlank() ? "" : (": " + responseBody)));
    this.statusCode = statusCode;
    this.uri = uri;
    this.responseBody = responseBody;
  }

  public int getStatusCode() { return statusCode; }
  public URI getUri() { return uri; }
  public String getResponseBody() { return responseBody; }
}

class VotingApiUnauthorizedException extends VotingApiHttpException {
  public VotingApiUnauthorizedException(URI uri, String body) { super(401, uri, body); }
}
class VotingApiForbiddenException extends VotingApiHttpException {
  public VotingApiForbiddenException(URI uri, String body) { super(403, uri, body); }
}
class VotingApiNotFoundException extends VotingApiHttpException {
  public VotingApiNotFoundException(URI uri, String body) { super(404, uri, body); }
}
class VotingApiValidationException extends VotingApiHttpException {
  public VotingApiValidationException(URI uri, String body) { super(400, uri, body); }
}
class VotingApiServerException extends VotingApiHttpException {
  public VotingApiServerException(int status, URI uri, String body) { super(status, uri, body); }
}
