package org.realityforge.gwt.webpoller.client;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An request factory that uses the standard GWT HTTP module.
 */
public abstract class AbstractHttpRequestFactory
  implements RequestFactory
{
  protected abstract RequestBuilder getRequestBuilder();

  @Nonnull
  @Override
  public Request newRequest( @Nonnull final RequestContext requestContext )
    throws Exception
  {
    final RequestBuilder requestBuilder = getRequestBuilder();
    requestBuilder.setCallback( new RequestCallback()
    {
      @Override
      public void onResponseReceived( final com.google.gwt.http.client.Request request, final Response response )
      {
        final Throwable error = getError( response );
        if ( null != error )
        {
          requestContext.onError( error );
        }
        else
        {
          final String data = response.getText();
          if ( 0 == data.length() )
          {
            requestContext.onEmptyMessage();
          }
          else
          {
            final HashMap<String, String> context = new HashMap<String, String>();
            for ( final Header header : response.getHeaders() )
            {
              context.put( header.getName(), header.getValue() );
            }
            requestContext.onMessage( context, response.getText() );
          }
        }
      }

      @Override
      public void onError( final com.google.gwt.http.client.Request request, final Throwable exception )
      {
        requestContext.onError( exception );
      }
    } );
    return new HttpRequest( requestBuilder.send() );
  }

  /**
   * Return error associated with response.
   *
   * @param response the response.
   * @return the error if any, null otherwise.
   */
  @Nullable
  protected Throwable getError( @Nonnull final Response response )
  {
    final int statusCode = response.getStatusCode();
    if ( Response.SC_OK == statusCode )
    {
      return null;
    }
    else
    {
      return new Exception( "Bad status code: " + statusCode );
    }
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
