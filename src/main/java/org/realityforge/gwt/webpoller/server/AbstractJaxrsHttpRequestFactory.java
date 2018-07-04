package org.realityforge.gwt.webpoller.server;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import org.realityforge.gwt.webpoller.client.Request;
import org.realityforge.gwt.webpoller.client.RequestContext;
import org.realityforge.gwt.webpoller.client.RequestFactory;

/**
 * An request factory that uses the standard Jaxrs client module.
 */
public abstract class AbstractJaxrsHttpRequestFactory
  implements RequestFactory
{
  @Nullable
  protected abstract Invocation.Builder getInvocation();

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public Request newRequest( @Nonnull final RequestContext requestContext )
    throws Exception
  {
    final Invocation.Builder builder = getInvocation();
    if( null == builder )
    {
      return null;
    }
    final Future<Response> future =
      builder.async().get( new InvocationCallback<Response>()
      {
        @Override
        public void completed( final Response response )
        {
          final Throwable error = getError( response );
          if ( null != error )
          {
            requestContext.onError( error );
          }
          else
          {
            final String data = response.readEntity( String.class );
            if ( null == data || 0 == data.length() )
            {
              requestContext.onEmptyMessage();
            }
            else
            {
              final HashMap<String, String> context = new HashMap<>();
              for ( final Map.Entry<String, List<Object>> header : response.getHeaders().entrySet() )
              {
                context.put( header.getKey(), header.getValue().get( 0 ).toString() );
              }
              requestContext.onMessage( context, data );
            }
          }
        }

        @Override
        public void failed( final Throwable throwable )
        {
          if ( throwable instanceof ProcessingException && throwable.getCause() instanceof SocketTimeoutException )
          {
            requestContext.onEmptyMessage();
          }
          else if ( throwable instanceof CancellationException )
          {
            requestContext.onEmptyMessage();
          }
          else
          {
            requestContext.onError( throwable );
          }
        }
      } );

    return new HttpRequest( future );
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
    final int statusCode = response.getStatus();
    if ( Response.Status.OK.getStatusCode() == statusCode )
    {
      return null;
    }
    else
    {
      return new Exception( "Bad status code: " + statusCode );
    }
  }

  /**
   * Adapter for Future to poller request.
   */
  private static class HttpRequest
    implements Request
  {
    private Future<?> _future;

    HttpRequest( @Nonnull final Future<?> future )
    {
      _future = future;
    }

    @Override
    public synchronized void cancel()
    {
      if ( null != _future )
      {
        _future.cancel( true );
        _future = null;
      }
    }
  }
}
