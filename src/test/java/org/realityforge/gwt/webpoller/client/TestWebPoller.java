package org.realityforge.gwt.webpoller.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import javax.annotation.Nonnull;

final class TestWebPoller
  extends WebPoller
{
  static class Factory
    implements WebPoller.Factory
  {
    @Override
    public WebPoller newWebPoller( final boolean longPoll )
    {
      return new TestWebPoller( new SimpleEventBus() );
    }
  }

  TestWebPoller( final EventBus eventBus )
  {
    super( eventBus );
  }

  @Override
  public void connect( @Nonnull final String server )
  {
  }

  @Override
  public void close()
  {
  }
}
