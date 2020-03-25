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

package com.io7m.jwheatsheaf.ui;

import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserType;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The default implementation of the {@link JWFileChoosersType} interface.
 */

public final class JWFileChoosers implements JWFileChoosersType
{
  private final ExecutorService ioExecutor;
  private final JWFileImageDefaultSet imageSet;
  private final JWStrings strings;

  private JWFileChoosers(
    final JWStrings inStrings,
    final ExecutorService inIoExecutor)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.ioExecutor =
      Objects.requireNonNull(inIoExecutor, "ioExecutor");
    this.imageSet =
      new JWFileImageDefaultSet();
  }

  /**
   * Create a new file chooser provider.
   *
   * @return A file chooser provider
   */

  public static JWFileChoosersType create()
  {
    final var executor =
      Executors.newSingleThreadExecutor(runnable -> {
        final var thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(
          String.format(
            "com.io7m.jwheatsheaf.ui.io[%d]",
            Long.valueOf(thread.getId())
          )
        );
        return thread;
      });

    return createWith(executor, Locale.getDefault());
  }

  /**
   * Create a new file chooser provider.
   *
   * @param executor An executor used for background I/O operations
   * @param locale   The locale used for internal string resources
   *
   * @return A file chooser provider
   */

  public static JWFileChoosersType createWith(
    final ExecutorService executor,
    final Locale locale)
  {
    final var strings = JWStrings.of(JWStrings.getResourceBundle(locale));
    return new JWFileChoosers(strings, executor);
  }

  ExecutorService ioExecutor()
  {
    return this.ioExecutor;
  }

  JWStrings strings()
  {
    return this.strings;
  }

  @Override
  public JWFileChooserType create(
    final Window window,
    final JWFileChooserConfiguration configuration)
  {
    Objects.requireNonNull(window, "window");
    Objects.requireNonNull(configuration, "configuration");

    try {
      final var chooserXML =
        JWFileChooser.class.getResource("chooser.fxml");
      Objects.requireNonNull(chooserXML, "chooserXML");

      final var resources = JWStrings.getResourceBundle();
      final var loader = new FXMLLoader(chooserXML, resources);
      final Pane pane = loader.load();

      configuration.cssStylesheet()
        .ifPresent(url -> pane.getStylesheets().add(url.toString()));

      final var viewController =
        (JWFileChooserViewController) loader.getController();
      viewController.setConfiguration(
        this,
        configuration
      );

      final var dialog = new Stage(((Stage) window).getStyle());
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setScene(new Scene(pane));

      switch (configuration.cardinality()) {
        case SINGLE: {
          dialog.setTitle(this.strings.fileSelect());
          break;
        }
        case MULTIPLE: {
          dialog.setTitle(this.strings.filesSelect());
          break;
        }
      }

      return new JWFileChooser(dialog, viewController);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close()
  {
    this.ioExecutor.shutdown();
  }

  JWFileImageSetType imageSet()
  {
    return this.imageSet;
  }
}
