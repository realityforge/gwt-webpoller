package org.realityforge.gwt.webpoller.client;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * The context passed to the factory when creating a request.
 */
public interface RequestContext
{
  /**
   * Method invoked when the poll fails to return any data to process.
   */
  void onEmptyMessage();

  /**
   * Method invoked when the poll returns some data.
   */
  void onMessage( @Nonnull Map<String,String> context, @Nonnull String data );

  /**
   * Method invoked when the poll results in an error.
   */
  void onError( @Nonnull Throwable exception );
}
