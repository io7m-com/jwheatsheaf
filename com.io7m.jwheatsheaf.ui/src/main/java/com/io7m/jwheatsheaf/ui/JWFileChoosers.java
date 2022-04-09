/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import com.io7m.jwheatsheaf.api.JWFileChooserType;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.ui.internal.JWFileChooserFilterAllFiles;
import com.io7m.jwheatsheaf.ui.internal.JWFileChooserFilterOnlyDirectories;
import com.io7m.jwheatsheaf.ui.internal.JWFileChooserViewController;
import com.io7m.jwheatsheaf.ui.internal.JWFileChoosersTesting;
import com.io7m.jwheatsheaf.ui.internal.JWFileImageDefaultSet;
import com.io7m.jwheatsheaf.ui.internal.JWStrings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
  private final JWFileChoosersTesting testing;
  private final ExecutorService ioExecutor;
  private final JWFileImageDefaultSet imageSet;
  private final JWStrings strings;
  private final JWFileChooserFilterType filterAllFiles;
  private final JWFileChooserFilterType filterOnlyDirectories;

  private JWFileChoosers(
    final JWStrings inStrings,
    final JWFileChoosersTesting inTesting,
    final ExecutorService inIoExecutor,
    final JWFileChooserFilterType inFilterAllFiles,
    final JWFileChooserFilterType inFilterOnlyDirectories)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.testing =
      Objects.requireNonNull(inTesting, "testing");
    this.ioExecutor =
      Objects.requireNonNull(inIoExecutor, "ioExecutor");
    this.filterAllFiles =
      Objects.requireNonNull(inFilterAllFiles, "filterAllFiles");
    this.filterOnlyDirectories =
      Objects.requireNonNull(inFilterOnlyDirectories, "filterOnlyDirectories");
    this.imageSet =
      new JWFileImageDefaultSet();
  }

  /**
   * @return The default icon set
   */

  public static JWFileImageSetType createDefaultIcons()
  {
    return new JWFileImageDefaultSet();
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
   * @param testing  Testing parameters
   * @param locale   The locale used for internal string resources
   *
   * @return A file chooser provider
   */

  public static JWFileChoosersType createWithTesting(
    final ExecutorService executor,
    final JWFileChoosersTesting testing,
    final Locale locale)
  {
    final var strings =
      JWStrings.of(JWStrings.getResourceBundle(locale));

    return new JWFileChoosers(
      strings,
      testing,
      executor,
      JWFileChooserFilterAllFiles.create(strings),
      JWFileChooserFilterOnlyDirectories.create(strings)
    );
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
    final var strings =
      JWStrings.of(JWStrings.getResourceBundle(locale));
    final var testing =
      JWFileChoosersTesting.builder()
        .build();

    return new JWFileChoosers(
      strings,
      testing,
      executor,
      JWFileChooserFilterAllFiles.create(strings),
      JWFileChooserFilterOnlyDirectories.create(strings)
    );
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
        JWFileChooser.class.getResource(
          "/com/io7m/jwheatsheaf/ui/internal/chooser.fxml");
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
        this.ioExecutor,
        this.testing,
        this.strings,
        this.imageSet,
        configuration
      );

      final var dialog = new Stage(((Stage) window).getStyle());
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initStyle(StageStyle.DECORATED);
      dialog.setScene(new Scene(pane));

      /*
       * Configure the title for the dialog.
       */

      configuration.title()
        .ifPresentOrElse(
          dialog::setTitle,
          () -> {
            switch (configuration.action()) {
              case CREATE:
              case OPEN_EXISTING_SINGLE:
                dialog.setTitle(this.strings.fileSelect());
                break;
              case OPEN_EXISTING_MULTIPLE:
                dialog.setTitle(this.strings.filesSelect());
                break;
            }
          }
        );

      /*
       * Close the dialog when escape is pressed.
       */

      dialog.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
        if (KeyCode.ESCAPE == event.getCode()) {
          dialog.close();
        }
      });

      return new JWFileChooser(dialog, viewController);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public JWFileChooserFilterType filterForAllFiles()
  {
    return this.filterAllFiles;
  }

  @Override
  public JWFileChooserFilterType filterForOnlyDirectories()
  {
    return this.filterOnlyDirectories;
  }

  @Override
  public void close()
  {
    this.ioExecutor.shutdown();
  }
}
