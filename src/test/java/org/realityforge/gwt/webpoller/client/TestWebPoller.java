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
    public WebPoller newWebPoller( @Nonnull final RequestFactory requestFactory )
    {
      return new TestWebPoller( new SimpleEventBus(), requestFactory );
    }
  }

  int _pollCount;

  TestWebPoller( final EventBus eventBus, @Nonnull final RequestFactory requestFactory )
  {
    super( eventBus, requestFactory );
  }

  @Override
  protected void doPoll()
  {
    _pollCount++;
  }
}
