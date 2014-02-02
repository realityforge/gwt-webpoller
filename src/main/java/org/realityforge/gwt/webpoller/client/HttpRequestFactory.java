package org.realityforge.gwt.webpoller.client;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import java.util.HashMap;
import javax.annotation.Nonnull;

/**
 * A request factory that uses the standard GWT HTTP module.
 */
public final class HttpRequestFactory
  implements RequestFactory
{
  private final RequestBuilder _requestBuilder;

  public HttpRequestFactory( @Nonnull final RequestBuilder requestBuilder )
  {
    _requestBuilder = requestBuilder;
  }

  @Nonnull
  @Override
  public Request newRequest( @Nonnull final RequestContext requestContext )
    throws Exception
  {
    _requestBuilder.setCallback( new RequestCallback()
    {
      @Override
      public void onResponseReceived( final com.google.gwt.http.client.Request request, final Response response )
      {
        final String data = response.getText();
        if ( 0 == data.length() )
        {
          requestContext.onEmptyMessage();
        }
        else
        {
          final HashMap<String, String> context = new HashMap<>();
          for ( final Header header : response.getHeaders() )
          {
            context.put( header.getName(), header.getValue() );
          }
          requestContext.onMessage( context, response.getText() );
        }
      }

      @Override
      public void onError( final com.google.gwt.http.client.Request request, final Throwable exception )
      {
        requestContext.onError( exception );
      }
    } );
    return new HttpRequest( _requestBuilder.send() );
  }

  /**
   * Adapter for HttpRequest to poller request.
   */
  private static class HttpRequest
    implements Request
  {
    private com.google.gwt.http.client.Request _request;

    private HttpRequest( final com.google.gwt.http.client.Request request )
    {
      _request = request;
    }

    @Override
    public void cancel()
    {
      if ( null != _request )
      {
        if ( _request.isPending() )
        {
          _request.cancel();
        }
        _request = null;
      }
    }
  }
}
