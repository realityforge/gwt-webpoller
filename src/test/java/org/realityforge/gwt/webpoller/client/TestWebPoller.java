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
    @Nonnull
    @Override
    public WebPoller newWebPoller( @Nonnull final RequestFactory requestFactory, final boolean longPoll )
    {
      return new TestWebPoller( new SimpleEventBus(), requestFactory, longPoll );
    }
  }

  TestWebPoller( final EventBus eventBus, @Nonnull final RequestFactory requestFactory, final boolean longPoll )
  {
    super( eventBus, requestFactory, longPoll );
  }

  @Override
  public void start()
  {

  }

  @Override
  public void stop()
  {
  }
}
