package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent.Handler;

/**
 * Event fired when there is an error with the web poller.
 */
public class ErrorEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onErrorEvent( @Nonnull ErrorEvent event );
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static Type<Handler> getType()
  {
    return TYPE;
  }

  private final Throwable _exception;

  public ErrorEvent( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
  {
    super( webPoller );
    _exception = exception;
  }

  @Nonnull
  public Throwable getException()
  {
    return _exception;
  }

  @Override
  public Type<Handler> getAssociatedType()
  {
    return ErrorEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onErrorEvent( this );
  }
}
