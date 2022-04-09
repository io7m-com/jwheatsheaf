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

package com.io7m.jwheatsheaf.examples;

import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserEventType;
import com.io7m.jwheatsheaf.api.JWFileChooserStringOverridesEmpty;
import com.io7m.jwheatsheaf.api.JWFileChooserStringOverridesType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.io7m.jwheatsheaf.ui.internal.JWFileChoosersTesting;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The view controller for the example application.
 */

public final class ExampleViewController implements Initializable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ExampleViewController.class);

  private ExampleFilesystems filesystems;
  private ExampleImageSets imageSets;

  @FXML
  private Pane main;
  @FXML
  private ChoiceBox<String> filesystem;
  @FXML
  private ChoiceBox<String> cssSelection;
  @FXML
  private ChoiceBox<String> imageSetSelection;
  @FXML
  private ComboBox<Locale> localeSelection;
  @FXML
  private TextArea textArea;
  @FXML
  private CheckBox allowDirectoryCreation;
  @FXML
  private CheckBox slowIO;
  @FXML
  private CheckBox homeDirectory;
  @FXML
  private CheckBox parentDirectory;
  @FXML
  private CheckBox confirmSelection;
  @FXML
  private CheckBox unusualStrings;
  @FXML
  private CheckBox onlyFiles;
  @FXML
  private ChoiceBox<JWFileChooserAction> action;
  @FXML
  private TextField title;

  /**
   * Construct a view controller.
   */

  public ExampleViewController()
  {

  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    Objects.requireNonNull(location, "location");
    Objects.requireNonNull(resources, "resources");

    try {
      this.filesystems = ExampleFilesystems.create();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    this.imageSets = ExampleImageSets.create();

    this.action.setItems(
      FXCollections.observableArrayList(JWFileChooserAction.values())
    );
    this.action.getSelectionModel().select(0);

    this.filesystem.setItems(
      FXCollections.observableList(
        this.filesystems.filesystems()
          .keySet()
          .stream()
          .sorted()
          .collect(Collectors.toList())
      )
    );
    this.filesystem.getSelectionModel().select(0);

    this.cssSelection.setItems(FXCollections.observableArrayList(
      "example.css",
      "olivebench.css"
    ));
    this.cssSelection.getSelectionModel().select(0);

    this.imageSetSelection.setItems(
      FXCollections.observableArrayList(
        this.imageSets.imageSets()
          .keySet()
          .stream()
          .sorted()
          .collect(Collectors.toList())
      )
    );
    this.imageSetSelection.getSelectionModel().select(0);

    this.localeSelection.setItems(
      FXCollections.observableArrayList(
        Arrays.stream(Locale.getAvailableLocales())
          .sorted(Comparator.comparing(Locale::toString))
          .collect(Collectors.toList())
      )
    );
    this.localeSelection.getSelectionModel().selectFirst();
  }

  @FXML
  private void onAllowDirectoryCreationChanged()
  {

  }

  @FXML
  private void onSlowIOChanged()
  {

  }

  @FXML
  private void onHomeDirectoryChanged()
  {

  }

  @FXML
  private void onParentDirectoryChanged()
  {

  }

  @FXML
  private void onConfirmSelectionChanged()
  {

  }

  @FXML
  private void onOpenSelected()
    throws IOException
  {
    final var fileSystem =
      this.filesystems.filesystems()
        .get(this.filesystem.getValue());
    final var imageSet =
      this.imageSets.imageSets()
        .get(this.imageSetSelection.getValue());

    final var recents =
      List.of(
        fileSystem.getPath("A", "B", "C"),
        fileSystem.getPath("D", "E", "F"),
        fileSystem.getPath("G", "H", "I")
      );

    final var configurationBuilder =
      JWFileChooserConfiguration.builder();

    final JWFileChooserStringOverridesType stringOverrides;
    if (this.unusualStrings.isSelected()) {
      stringOverrides = new ExampleWeirdStrings();
      configurationBuilder.setTitle("Please deblaterate on your limerance!");
    } else {
      stringOverrides = JWFileChooserStringOverridesEmpty.get();
    }

    final Function<Path, Boolean> fileSelectionMode;
    if (this.onlyFiles.isSelected()) {
      fileSelectionMode = path -> Boolean.valueOf(Files.isRegularFile(path));
    } else {
      fileSelectionMode = path -> Boolean.TRUE;
    }

    configurationBuilder
      .setAllowDirectoryCreation(this.allowDirectoryCreation.isSelected())
      .setShowParentDirectory(this.parentDirectory.isSelected())
      .setConfirmFileSelection(this.confirmSelection.isSelected())
      .setFileSystem(fileSystem)
      .setCssStylesheet(ExampleViewController.class.getResource(this.cssSelection.getValue()))
      .setFileImageSet(imageSet)
      .setAction(this.action.getValue())
      .setStringOverrides(stringOverrides)
      .addFileFilters(new ExampleFilterRejectAll())
      .addFileFilters(new ExampleFilterXML())
      .setFileSelectionMode(fileSelectionMode)
      .addAllRecentFiles(recents);

    if (this.homeDirectory.isSelected()) {
      configurationBuilder.setHomeDirectory(
        fileSystem.getPath(System.getProperty("user.home"))
      );
    }

    if (!this.title.getText().isEmpty()) {
      configurationBuilder.setTitle(this.title.getText());
    }

    final var configuration =
      configurationBuilder.build();

    final var testingBuilder = JWFileChoosersTesting.builder();
    if (this.slowIO.isSelected()) {
      testingBuilder.setIoDelay(Duration.of(2L, ChronoUnit.SECONDS));
    }
    final var testing =
      testingBuilder.build();
    final var locale =
      this.localeSelection.getSelectionModel().getSelectedItem();

    LOG.debug("Locale: {}", locale);

    try (var choosers = JWFileChoosers.createWithTesting(
      Executors.newSingleThreadExecutor(),
      testing,
      locale
    )) {
      final var chooser =
        choosers.create(this.main.getScene().getWindow(), configuration);

      chooser.setEventListener(event -> {
        if (event instanceof JWFileChooserEventType.JWFileChooserEventErrorType) {
          final var alert = new Alert(Alert.AlertType.ERROR);
          final var error = (JWFileChooserEventType.JWFileChooserEventErrorType) event;
          alert.setContentText(error.path() + ": " + error.exception());
          alert.show();
        }
      });

      final List<Path> selected = chooser.showAndWait();
      this.textArea.setText(
        selected.stream()
          .map(Path::toString)
          .collect(Collectors.joining(System.lineSeparator()))
      );
    }
  }
}
