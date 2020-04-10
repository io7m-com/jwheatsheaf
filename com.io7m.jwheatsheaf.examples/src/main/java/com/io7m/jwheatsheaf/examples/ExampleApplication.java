/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jwheatsheaf.examples;

import com.io7m.jwheatsheaf.ui.internal.JWStrings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExampleApplication extends Application
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ExampleApplication.class);

  /**
   * Construct an application.
   */

  public ExampleApplication()
  {

  }

  @Override
  public void start(final Stage stage)
    throws Exception
  {
    LOG.debug("starting application");

    final var mainXML =
      ExampleApplication.class.getResource("example.fxml");
    final var loader =
      new FXMLLoader(mainXML, JWStrings.getResourceBundle());

    final Pane pane = loader.load();
    pane.getStylesheets().add(
      ExampleApplication.class.getResource("example.css").toString());

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.titleProperty().setValue("JWheatsheaf Example");
    stage.show();
  }
}
