package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.MessageEvent.Handler;

public class MessageEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onMessageEvent( @Nonnull MessageEvent event );
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static Type<Handler> getType()
  {
    return TYPE;
  }

  private final Map<String,String> _context;
  private final String _data;

  public MessageEvent( @Nonnull final WebPoller webPoller,
                       @Nonnull final Map<String,String> context,
                       @Nonnull final String data )
  {
    super( webPoller );
    _context = Collections.unmodifiableMap( context );
    _data = data;
  }

  @Nonnull
  public Map<String, String> getContext()
  {
    return _context;
  }

  @Nonnull
  public String getData()
  {
    return _data;
  }

  @Override
  public Type<Handler> getAssociatedType()
  {
    return MessageEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onMessageEvent( this );
  }
}
