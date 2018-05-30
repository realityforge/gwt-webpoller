package org.realityforge.gwt.webpoller.client;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public final class CancelableRequestFactory
  implements RequestFactory
{
  @Nonnull
  private final RequestFactory _factory;
  @Nonnull
  private final Consumer<Runnable> _wrapper;

  public CancelableRequestFactory( @Nonnull final RequestFactory factory, @Nonnull final Consumer<Runnable> wrapper )
  {
    _factory = Objects.requireNonNull( factory );
    _wrapper = Objects.requireNonNull( wrapper );
  }

  @Nonnull
  @Override
  public Request newRequest( @Nonnull final RequestContext context )
    throws Exception
  {
    final CancelableRequestWrapper request = new CancelableRequestWrapper();
    _wrapper.accept( () -> bindRequest( context, request ) );
    return request;
  }

  private void bindRequest( @Nonnull final RequestContext context, @Nonnull final CancelableRequestWrapper request )
  {
    try
    {
      request.setRequest( _factory.newRequest( context ) );
    }
    catch ( final Exception ignored )
    {
      // Fine to ignore as we assume it is effectively canceled
    }
  }
}
