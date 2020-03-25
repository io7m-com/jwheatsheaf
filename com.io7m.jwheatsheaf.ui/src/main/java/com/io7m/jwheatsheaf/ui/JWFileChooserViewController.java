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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jwheatsheaf.api.JWDirectoryCreationFailed;
import com.io7m.jwheatsheaf.api.JWFileListingFailed;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserEventType;
import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.api.JWFileKind;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The file chooser view controller.
 *
 * The {@link #setConfiguration(JWFileChoosers, JWFileChooserConfiguration)} method
 * must be called after creation.
 */

public final class JWFileChooserViewController
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileChooserViewController.class);

  private final ChangeListener<Path> listener;
  private JWFileChooserConfiguration configuration;
  private JWFileChooserFilterType filterAll;
  private JWFileChooserFilterType filterOnlyDirectories;
  private JWFileChoosers choosers;
  private JWFileImageSetType imageSet;
  private JWFileList fileListing;
  private List<Path> result;
  private volatile Path currentDirectory;
  private final AtomicReference<Consumer<JWFileChooserEventType>> eventReceiver;

  @FXML
  private Pane mainContent;
  @FXML
  private ChoiceBox<Path> pathMenu;
  @FXML
  private TableView<JWFileItem> directoryTable;
  @FXML
  private Button newDirectoryButton;
  @FXML
  private Button upDirectoryButton;
  @FXML
  private ListView<JWFileSourceEntryType> sourcesList;
  @FXML
  private ComboBox<JWFileChooserFilterType> fileTypeMenu;
  @FXML
  private TextField fileName;
  @FXML
  private Button okButton;
  @FXML
  private TextField searchField;

  /**
   * Construct a view controller.
   */

  public JWFileChooserViewController()
  {
    this.listener = this::onPathMenuItemSelected;
    this.result = List.of();
    this.eventReceiver = new AtomicReference<>(event -> {});
  }

  private static List<Path> allParentsOf(
    final Path startDirectory)
  {
    final var parents = new ArrayList<Path>(startDirectory.getNameCount());
    parents.add(startDirectory);
    var currentDirectory = startDirectory.getParent();
    while (currentDirectory != null) {
      parents.add(currentDirectory);
      currentDirectory = currentDirectory.getParent();
    }
    return parents;
  }

  private String formatSize(
    final long size)
  {
    return this.configuration.fileSizeFormatter().formatSize(size);
  }

  public void setEventReceiver(
    final Consumer<JWFileChooserEventType> eventReceiver)
  {
    this.eventReceiver.set(
      Objects.requireNonNullElse(eventReceiver, event -> {}));
  }

  /**
   * Set the file chooser provider and configuration.
   *  @param inChoosers      The provider
   * @param inConfiguration The configuration
   */

  public void setConfiguration(
    final JWFileChoosers inChoosers,
    final JWFileChooserConfiguration inConfiguration)
  {
    this.choosers =
      Objects.requireNonNull(inChoosers, "inChoosers");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");

    this.filterAll =
      JWFileChooserFilterAllFiles.create(this.choosers.strings());
    this.filterOnlyDirectories =
      JWFileChooserFilterOnlyDirectories.create(this.choosers.strings());
    this.fileListing =
      new JWFileList(this.filterAll);

    this.imageSet =
      this.configuration.fileImageSet()
        .orElse(this.choosers.imageSet());

    this.newDirectoryButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forDirectoryCreate())
    );
    this.upDirectoryButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forDirectoryUp())
    );

    final var fileSystem =
      this.configuration.fileSystem();

    final var startDirectory =
      this.configuration.initialDirectory()
        .orElse(fileSystem.getPath("").toAbsolutePath());

    this.configureButtons();
    this.configureSearch();
    this.configureTableView();
    this.configureFileTypeMenu();
    this.configureSourceList(fileSystem);
    this.setCurrentDirectory(startDirectory);
  }

  private void configureSearch()
  {
    this.searchField.focusedProperty()
      .addListener(observable -> this.onSearchFieldChanged());
  }

  private void configureSourceList(
    final FileSystem fileSystem)
  {
    /*
     * Add a set of source entries. The list begins with the
     * recent items source, and then contains one entry per filesystem
     * root.
     */

    final var sources = new ArrayList<JWFileSourceEntryType>();
    sources.add(new JWFileSourceEntryRecentItems(this.configuration));
    for (final var root : fileSystem.getRootDirectories()) {
      sources.add(new JWFileSourceEntryFilesystemRoot(root));
    }

    this.sourcesList.setItems(FXCollections.observableList(sources));
    this.sourcesList.setCellFactory(param -> {
      final ListCell<JWFileSourceEntryType> cell = new ListCell<>()
      {
        @Override
        protected void updateItem(
          final JWFileSourceEntryType item,
          final boolean empty)
        {
          super.updateItem(item, empty);

          if (empty || item == null) {
            this.setGraphic(null);
            this.setText(null);
            return;
          }

          item.onListCell(
            JWFileChooserViewController.this.imageSet,
            JWFileChooserViewController.this.choosers.strings(),
            this
          );
        }
      };

      cell.setOnMouseClicked(this::onSourceItemDoubleClicked);
      return cell;
    });
  }

  private void configureFileTypeMenu()
  {
    final Callback<ListView<JWFileChooserFilterType>, ListCell<JWFileChooserFilterType>> cellFactory =
      param -> {
        final ListCell<JWFileChooserFilterType> cell = new ListCell<>()
        {
          @Override
          protected void updateItem(
            final JWFileChooserFilterType item,
            final boolean empty)
          {
            super.updateItem(item, empty);

            if (empty || item == null) {
              this.setGraphic(null);
              this.setText(null);
              return;
            }

            this.setText(item.description());
            this.setGraphic(null);
          }
        };
        return cell;
      };

    this.fileTypeMenu.setCellFactory(cellFactory);
    this.fileTypeMenu.setButtonCell(cellFactory.call(null));

    final var filters = new ArrayList<JWFileChooserFilterType>();
    filters.add(this.filterAll);
    filters.add(this.filterOnlyDirectories);
    filters.addAll(this.configuration.fileFilters());
    this.fileTypeMenu.setItems(FXCollections.observableList(filters));
    this.fileTypeMenu.getSelectionModel().select(0);
  }

  /**
   * Set the current directory. This has the effect of rebuilding the
   * path menu at the top of the file chooser, and populating the directory
   * table.
   *
   * @param path The new directory
   */

  private void setCurrentDirectory(
    final Path path)
  {
    this.currentDirectory = Objects.requireNonNull(path, "path");
    this.rebuildPathMenu(path);
    this.populateDirectoryTable(path);
  }

  private void rebuildPathMenu(
    final Path startDirectory)
  {
    final var directories = allParentsOf(startDirectory);
    final var selectionModel = this.pathMenu.getSelectionModel();
    selectionModel.selectedItemProperty().removeListener(this.listener);
    this.pathMenu.setItems(FXCollections.observableList(directories));
    selectionModel.select(0);
    selectionModel.selectedItemProperty().addListener(this.listener);
  }

  private void populateDirectoryTable(
    final Path directory)
  {
    this.populateDirectoryTableWith(() -> JWFileItems.listDirectory(directory));
  }

  private void populateDirectoryTableWith(
    final JWFileListingRetrieverType itemRetriever)
  {
    this.directoryTable.setItems(this.fileListing.items());

    this.choosers.ioExecutor().execute(() -> {
      try {
        final var items = itemRetriever.onFileItemsRequested();
        Platform.runLater(() -> this.fileListing.setItems(items));
      } catch (final Exception e) {
        LOG.error("exception during directory listing: ", e);
        Platform.runLater(() -> {
          this.fileListing.setItems(List.of());
          try {
            this.eventReceiver.get()
              .accept(JWFileListingFailed.of(this.currentDirectory, e));
          } catch (final Exception ex) {
            LOG.error("exception raised by event receiver: ", ex);
          }
        });
      }
    });
  }

  private void onPathMenuItemSelected(
    final ObservableValue<? extends Path> observable,
    final Path oldValue,
    final Path newValue)
  {
    this.setCurrentDirectory(newValue);
  }

  @FXML
  private void onFileFilterSelected()
  {
    this.fileListing.setFilter(this.fileTypeMenu.getValue());
  }

  @FXML
  private void onSearchFieldChanged()
  {
    this.fileListing.setSearch(this.searchField.getText().trim());
  }

  @FXML
  private void onUpDirectoryButton()
  {
    final var parent = this.currentDirectory.getParent();
    if (parent != null) {
      this.setCurrentDirectory(parent);
    }
  }

  @FXML
  private void onCreateDirectoryButton()
  {
    final var dialog = new TextInputDialog();
    dialog.setHeaderText(this.choosers.strings().createDirectoryTitle());
    dialog.setContentText(this.choosers.strings().enterDirectoryName());
    final var nameOpt = dialog.showAndWait();
    if (nameOpt.isPresent()) {
      final var name = nameOpt.get();
      final var newDirectory = this.currentDirectory.resolve(name);
      try {
        Files.createDirectories(newDirectory);
      } catch (final IOException e) {
        LOG.error("error creating directory: ", e);
        try {
          this.eventReceiver.get()
            .accept(JWDirectoryCreationFailed.of(newDirectory, e));
        } catch (final Exception ex) {
          LOG.error("exception raised by event receiver: ", ex);
        }
      }
      this.setCurrentDirectory(this.currentDirectory);
    }
  }

  @FXML
  private void onOKSelected()
  {
    this.result =
      this.directoryTable.getSelectionModel()
        .getSelectedItems()
        .stream()
        .map(JWFileItem::path)
        .collect(Collectors.toList());

    final var window = this.mainContent.getScene().getWindow();
    window.hide();
  }

  @FXML
  private void onCancelSelected()
  {
    this.result = List.of();

    final var window = this.mainContent.getScene().getWindow();
    window.hide();
  }

  private void configureButtons()
  {
    this.okButton.setDisable(true);

    this.newDirectoryButton.setDisable(
      !this.configuration.allowDirectoryCreation());

    final var selectionModel = this.directoryTable.getSelectionModel();
    selectionModel.selectedItemProperty()
      .addListener(item -> this.onDirectoryTableSelectedItemChanged());
  }

  private void configureTableView()
  {
    final var selectionModel = this.directoryTable.getSelectionModel();
    switch (this.configuration.cardinality()) {
      case SINGLE: {
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        break;
      }
      case MULTIPLE: {
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        break;
      }
    }

    this.directoryTable.setPlaceholder(new Label(""));

    final var tableColumns = this.directoryTable.getColumns();
    final TableColumn<JWFileItem, JWFileKind> tableTypeColumn =
      (TableColumn<JWFileItem, JWFileKind>) tableColumns.get(0);
    final TableColumn<JWFileItem, JWFileItem> tableNameColumn =
      (TableColumn<JWFileItem, JWFileItem>) tableColumns.get(1);
    final TableColumn<JWFileItem, Long> tableSizeColumn =
      (TableColumn<JWFileItem, Long>) tableColumns.get(2);
    final TableColumn<JWFileItem, FileTime> tableTimeColumn =
      (TableColumn<JWFileItem, FileTime>) tableColumns.get(3);

    tableTypeColumn.setSortable(false);
    tableTypeColumn.setReorderable(false);
    tableTypeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, JWFileKind> cell = new TableCell<>()
      {
        @Override
        protected void updateItem(
          final JWFileKind item,
          final boolean empty)
        {
          super.updateItem(item, empty);

          if (empty || item == null) {
            this.setGraphic(null);
            this.setText(null);
            this.setTooltip(null);
            return;
          }

          this.setGraphic(JWFileChooserViewController.this.imageOfKind(item));
          this.setText(null);
          this.setTooltip(JWFileChooserViewController.this.tooltipOf(item));
        }
      };

      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableNameColumn.setSortable(true);
    tableNameColumn.setReorderable(false);
    tableNameColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, JWFileItem> cell = new TableCell<>()
      {
        @Override
        protected void updateItem(
          final JWFileItem item,
          final boolean empty)
        {
          super.updateItem(item, empty);

          if (empty || item == null) {
            this.setGraphic(null);
            this.setText(null);
            this.setTooltip(null);
            return;
          }

          this.setGraphic(null);
          this.setText(item.name());
          this.setTooltip(JWFileChooserViewController.this.tooltipOf(item.kind()));
        }
      };
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableSizeColumn.setSortable(true);
    tableSizeColumn.setReorderable(false);
    tableSizeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, Long> cell = new TableCell<>()
      {
        @Override
        protected void updateItem(
          final Long item,
          final boolean empty)
        {
          super.updateItem(item, empty);

          if (empty || item == null) {
            this.setGraphic(null);
            this.setText(null);
            return;
          }

          this.setText(JWFileChooserViewController.this.formatSize(item.longValue()));
          this.setGraphic(null);
        }
      };
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableTimeColumn.setSortable(true);
    tableTimeColumn.setReorderable(false);
    tableTimeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, FileTime> cell = new TableCell<>()
      {
        @Override
        protected void updateItem(
          final FileTime item,
          final boolean empty)
        {
          super.updateItem(item, empty);

          if (empty || item == null) {
            this.setGraphic(null);
            this.setText(null);
            return;
          }

          this.setText(JWFileChooserViewController.this.formatTime(item));
          this.setGraphic(null);
        }
      };
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableTypeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue().kind()));
    tableNameColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableSizeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(Long.valueOf(param.getValue().size())));
    tableTimeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue().modifiedTime()));
  }

  private Tooltip tooltipOf(
    final JWFileKind kind)
  {
    switch (kind) {
      case REGULAR_FILE:
      case SYMBOLIC_LINK:
      case UNKNOWN:
        return null;
      case DIRECTORY:
        return new Tooltip(this.choosers.strings().tooltipNavigateDirectory());
    }
    throw new UnreachableCodeException();
  }

  private void onDirectoryTableSelectedItemChanged()
  {
    final var selectionModel = this.directoryTable.getSelectionModel();
    this.okButton.setDisable(selectionModel.getSelectedItems().size() < 1);
  }

  private void onTableRowClicked(
    final MouseEvent event)
  {
    if (event.getClickCount() == 1) {
      final var selectionModel = this.directoryTable.getSelectionModel();
      final var item = selectionModel.getSelectedItem();
      if (item != null) {
        this.fileName.setText(item.name());
      }
      return;
    }

    if (event.getClickCount() == 2) {
      final var selectionModel = this.directoryTable.getSelectionModel();
      final var item = selectionModel.getSelectedItem();
      if (item != null) {
        final var directory = item.path();
        if (Files.isDirectory(directory)) {
          this.setCurrentDirectory(directory);
        }
      }
    }
  }

  private void onSourceItemDoubleClicked(
    final MouseEvent event)
  {
    if (event.getClickCount() == 2) {
      final var item = this.sourcesList.getSelectionModel().getSelectedItem();
      item.path().ifPresent(this::rebuildPathMenu);
      this.populateDirectoryTableWith(item);
    }
  }

  private ImageView imageOfKind(
    final JWFileKind kind)
  {
    final var imageOpt = this.imageSet.forFileKind(kind);
    if (imageOpt.isPresent()) {
      final var imageView = new ImageView();
      imageView.setFitWidth(16.0);
      imageView.setFitHeight(16.0);
      imageView.setImage(new Image(imageOpt.get().toString()));
      return imageView;
    }
    return null;
  }

  private String formatTime(
    final FileTime item)
  {
    final var instant = item.toInstant();
    final var time = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    return this.configuration.fileTimeFormatter().format(time);
  }

  /**
   * @return The list of selected files, if any
   */

  public List<Path> result()
  {
    return this.result;
  }
}
