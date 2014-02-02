package org.realityforge.gwt.webpoller.client;

import com.google.gwt.http.client.RequestBuilder;
import javax.annotation.Nonnull;

/**
 * A request factory that uses the standard GWT HTTP module.
 */
public final class HttpRequestFactory
  extends AbstractHttpRequestFactory
  implements RequestFactory
{
  private final RequestBuilder _requestBuilder;

  public HttpRequestFactory( @Nonnull final RequestBuilder requestBuilder )
  {
    _requestBuilder = requestBuilder;
  }

  protected final RequestBuilder getRequestBuilder()
  {
    return _requestBuilder;
  }
}
