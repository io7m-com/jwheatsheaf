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

import com.io7m.jwheatsheaf.api.JWFileChooserCardinality;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserEventType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class ExampleViewController implements Initializable
{
  private ExampleFilesystems filesystems;
  private ExampleImageSets imageSets;

  @FXML
  private Pane main;
  @FXML
  private ChoiceBox<JWFileChooserCardinality> cardinality;
  @FXML
  private ChoiceBox<String> filesystem;
  @FXML
  private ChoiceBox<String> cssSelection;
  @FXML
  private ChoiceBox<String> imageSetSelection;
  @FXML
  private TextArea textArea;
  @FXML
  private CheckBox allowDirectoryCreation;

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

    this.cardinality.setItems(
      FXCollections.observableArrayList(JWFileChooserCardinality.values())
    );
    this.cardinality.getSelectionModel().select(0);

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
  }

  @FXML
  private void onAllowDirectoryCreationChanged()
  {

  }

  @FXML
  private void onOpenSelected()
  {
    final var configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(this.allowDirectoryCreation.isSelected())
        .setCardinality(this.cardinality.getValue())
        .setFileSystem(this.filesystems.filesystems().get(this.filesystem.getValue()))
        .setCssStylesheet(ExampleViewController.class.getResource(this.cssSelection.getValue()))
        .setFileImageSet(this.imageSets.imageSets().get(this.imageSetSelection.getValue()))
        .addFileFilters(new ExampleFilterRejectAll())
        .addFileFilters(new ExampleFilterXML())
        .build();

    final var choosers = JWFileChoosers.create();
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
