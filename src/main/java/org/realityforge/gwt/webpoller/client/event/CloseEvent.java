package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.CloseEvent.Handler;

/**
 * Event fired when the web poller is closed.
 */
public class CloseEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onCloseEvent( @Nonnull CloseEvent event );
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static Type<Handler> getType()
  {
    return TYPE;
  }

  public CloseEvent( @Nonnull final WebPoller webPoller )
  {
    super( webPoller );
  }

  @Override
  public Type<Handler> getAssociatedType()
  {
    return CloseEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onCloseEvent( this );
  }
}
