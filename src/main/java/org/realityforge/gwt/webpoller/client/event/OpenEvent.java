package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.OpenEvent.Handler;

/**
 * Event fired when the web poller connects for the first time.
 */
public class OpenEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onOpenEvent( @Nonnull OpenEvent event );
  }

  private static final GwtEvent.Type<Handler> TYPE = new Type<>();

  public static GwtEvent.Type<Handler> getType()
  {
    return TYPE;
  }

  public OpenEvent( @Nonnull final WebPoller webPoller )
  {
    super( webPoller );
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType()
  {
    return OpenEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onOpenEvent( this );
  }
}
