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

package com.io7m.jwheatsheaf.ui.internal;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jwheatsheaf.api.JWDirectoryCreationFailed;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserEventType;
import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.api.JWFileListingFailed;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;

/**
 * The file chooser view controller.
 *
 * The {@code setConfiguration} method must be called after creation.
 */

public final class JWFileChooserViewController
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileChooserViewController.class);

  private static final Runnable AND_THEN_DO_NOTHING = () -> {

  };

  private final AtomicReference<Consumer<JWFileChooserEventType>> eventReceiver;
  private final BlockingDeque<String> initialFilename;
  private final ChangeListener<Path> listener;
  @FXML private Button homeDirectoryButton;
  @FXML private Button newDirectoryButton;
  @FXML private Button okButton;
  @FXML private Button selectDirectButton;
  @FXML private Button upDirectoryButton;
  @FXML private ChoiceBox<Path> pathMenu;
  @FXML private ComboBox<JWFileChooserFilterType> fileTypeMenu;
  @FXML private ListView<JWFileSourceEntryType> sourcesList;
  @FXML private Pane mainContent;
  @FXML private ProgressIndicator progressIndicator;
  @FXML private TableView<JWFileItem> directoryTable;
  @FXML private TextField fileName;
  @FXML private TextField searchField;
  private ExecutorService ioExecutor;
  private JWFileChooserConfiguration configuration;
  private JWFileChooserFilterType filterAll;
  private JWFileChooserFilterType filterOnlyDirectories;
  private JWFileChoosers choosers;
  private JWFileChoosersTesting testing;
  private JWFileImageSetType imageSet;
  private JWFileList fileListing;
  private JWStrings strings;
  private JWToolTips toolTips;
  private List<Node> lockableViews;
  private volatile List<Path> result;
  private TableView.TableViewSelectionModel<JWFileItem> directoryTableSelectionModel;
  private volatile Path currentDirectory;

  /**
   * Construct a view controller.
   */

  public JWFileChooserViewController()
  {
    this.listener = this::onPathMenuItemSelected;
    this.result = List.of();
    this.initialFilename = new LinkedBlockingDeque<>();
    this.eventReceiver = new AtomicReference<>(event -> {
    });
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

  /**
   * Set a function that will receive events from the chooser.
   *
   * @param newEventReceiver The event receiver
   */

  public void setEventReceiver(
    final Consumer<JWFileChooserEventType> newEventReceiver)
  {
    this.eventReceiver.set(
      Objects.requireNonNullElse(newEventReceiver, event -> {
      }));
  }

  /**
   * Set the file chooser provider and configuration.
   *
   * @param inChoosers        The provider
   * @param inIoExecutor      An executor for background I/O operations
   * @param inTesting         An internal testing interface
   * @param inStrings         UI strings
   * @param inDefaultImageSet The default image set
   * @param inConfiguration   The configuration
   */

  public void setConfiguration(
    final JWFileChoosers inChoosers,
    final ExecutorService inIoExecutor,
    final JWFileChoosersTesting inTesting,
    final JWStrings inStrings,
    final JWFileImageSetType inDefaultImageSet,
    final JWFileChooserConfiguration inConfiguration)
  {
    this.choosers =
      Objects.requireNonNull(inChoosers, "inChoosers");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.testing =
      Objects.requireNonNull(inTesting, "inTesting");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.ioExecutor =
      Objects.requireNonNull(inIoExecutor, "inIoExecutor");

    Objects.requireNonNull(inDefaultImageSet, "inDefaultImageSet");

    this.directoryTableSelectionModel =
      this.directoryTable.getSelectionModel();
    this.toolTips =
      new JWToolTips(this.strings);

    this.filterAll =
      JWFileChooserFilterAllFiles.create(this.strings);
    this.filterOnlyDirectories =
      JWFileChooserFilterOnlyDirectories.create(this.strings);
    this.fileListing =
      new JWFileList(this.filterAll);

    this.imageSet =
      this.configuration.fileImageSet()
        .orElse(inDefaultImageSet);

    this.newDirectoryButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forDirectoryCreate())
    );
    this.upDirectoryButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forDirectoryUp())
    );
    this.selectDirectButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forSelectDirect())
    );
    this.homeDirectoryButton.setGraphic(
      JWImages.imageView16x16Of(this.imageSet.forHome())
    );

    final var fileSystem =
      this.configuration.fileSystem();

    final var startDirectory =
      this.configuration.initialDirectory()
        .orElse(fileSystem.getPath("").toAbsolutePath());

    this.configureButtons();
    this.configureSearch();
    this.configureFileField();
    this.configureTableView();
    this.configureFileTypeMenu();
    this.configureSourceList(fileSystem);

    /*
     * The list of views that will be "locked" when an I/O operation is
     * happening.
     */

    this.lockableViews = List.of(
      this.directoryTable,
      this.homeDirectoryButton,
      this.newDirectoryButton,
      this.okButton,
      this.pathMenu,
      this.searchField,
      this.selectDirectButton,
      this.sourcesList,
      this.upDirectoryButton
    );

    this.setCurrentDirectory(startDirectory, AND_THEN_DO_NOTHING);
  }

  private void configureSearch()
  {
    this.searchField.textProperty()
      .addListener(observable -> this.onSearchFieldChanged());
  }

  private void configureFileField()
  {
    this.fileName.textProperty()
      .addListener(observable -> this.onNameFieldChanged());
    this.configuration.initialFileName()
      .ifPresent(this.initialFilename::push);
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
      sources.add(new JWFileSourceEntryFilesystemRoot(
        this.configuration.showParentDirectory(), root)
      );
    }

    this.sourcesList.setItems(FXCollections.observableList(sources));
    this.sourcesList.setCellFactory(new SourceListCellFactory());
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

    /*
     * Select the default filter. If there isn't one, select the first
     * filter.
     */

    final var selectionModel =
      this.fileTypeMenu.getSelectionModel();

    this.configuration.fileFilterDefault()
      .ifPresentOrElse(
        selectionModel::select,
        () -> selectionModel.select(0)
      );

    this.onFileFilterSelected();
  }

  /**
   * Set the current directory. This has the effect of rebuilding the
   * path menu at the top of the file chooser, and populating the directory
   * table.
   *
   * @param path The new directory
   */

  private void setCurrentDirectory(
    final Path path,
    final Runnable andThen)
  {
    this.currentDirectory = Objects.requireNonNull(path, "path");
    this.rebuildPathMenu(path);
    this.populateDirectoryTable(path, andThen);
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
    final Path directory,
    final Runnable andThen)
  {
    this.populateDirectoryTableWith(
      () -> JWFileItems.listDirectory(
        directory,
        this.configuration.showParentDirectory()),
      andThen
    );
  }

  private void populateDirectoryTableWith(
    final JWFileListingRetrieverType itemRetriever,
    final Runnable andThen)
  {
    this.directoryTable.setItems(this.fileListing.items());
    this.ioLockUI();

    this.ioExecutor.execute(() -> {
      try {
        final var items = itemRetriever.onFileItemsRequested();
        this.applyTestingIODelayIfRequested();
        Platform.runLater(() -> {
          this.ioUnlockUI();
          this.fileListing.setItems(items);

          try {
            final var name = this.initialFilename.pop();
            this.trySelectDirectoryItem(items, name);
            this.fileName.setText(name);
          } catch (final NoSuchElementException e) {
            // Most of the time, there's no initial filename.
          }

          andThen.run();
        });
      } catch (final Exception e) {
        LOG.error("exception during directory listing: ", e);
        Platform.runLater(() -> {
          this.ioUnlockUI();
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

  /**
   * Select the item in the list of file items that has the given name and
   * return it. If none of them have the given name, do nothing and return
   * nothing.
   */

  private Optional<JWFileItem> trySelectDirectoryItem(
    final List<JWFileItem> items,
    final String name)
  {
    for (final var item : items) {

      /*
       * Check to see if the given name matches the display name of the file.
       * Some file items have artificial display names (such as "." and "..").
       */

      final var itemDisplayOpt = item.displayName();
      if (itemDisplayOpt.isPresent()) {
        final var itemDisplay = itemDisplayOpt.get();
        if (Objects.equals(itemDisplay, name)) {
          this.directoryTableSelectionModel.select(item);
          return Optional.of(item);
        }
      }

      /*
       * Check to see if the given name matches the filename component
       * of the file item path.
       */

      final var itemFile =
        Optional.ofNullable(item.path().getFileName())
          .map(Path::toString)
          .orElse(null);

      if (Objects.equals(itemFile, name)) {
        this.directoryTableSelectionModel.select(item);
        return Optional.of(item);
      }
    }
    return Optional.empty();
  }

  /**
   * Select the item in the list of file items that has the given name and
   * return it. If none of them have the given name, do nothing and clear
   * the directory table selection.
   */

  private Optional<JWFileItem> trySelectDirectoryItemOrDeselect(
    final TableView<JWFileItem> tableView,
    final String name)
  {
    final var selected =
      this.trySelectDirectoryItem(tableView.getItems(), name);

    if (selected.isEmpty()) {
      this.directoryTableSelectionModel.clearSelection();
    }
    return selected;
  }

  private void ioUnlockUI()
  {
    Preconditions.checkPreconditionV(
      Platform.isFxApplicationThread(),
      "Must be the FX application thread");

    for (final var view : this.lockableViews) {
      view.setDisable(false);
    }
    this.progressIndicator.setVisible(false);
    this.reconfigureOKButton();
  }

  private void ioLockUI()
  {
    Preconditions.checkPreconditionV(
      Platform.isFxApplicationThread(),
      "Must be the FX application thread");

    for (final var view : this.lockableViews) {
      view.setDisable(true);
    }

    this.progressIndicator.setVisible(true);
  }

  private void applyTestingIODelayIfRequested()
  {
    this.testing
      .ioDelay()
      .ifPresent(duration -> {
        try {
          Thread.sleep(duration.toMillis());
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
  }

  private void onPathMenuItemSelected(
    final ObservableValue<? extends Path> observable,
    final Path oldValue,
    final Path newValue)
  {
    this.setCurrentDirectory(newValue, AND_THEN_DO_NOTHING);
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
  private void onSelectDirectButton()
  {
    final var dialog = new TextInputDialog();
    dialog.setTitle(this.strings.enterPathTitle());
    dialog.setHeaderText(null);
    dialog.setContentText(this.strings.enterPath());
    dialog.getEditor().setId("fileChooserDialogSelectDirectTextField");

    this.configuration.cssStylesheet().ifPresent(css -> {
      dialog.getDialogPane()
        .getStylesheets()
        .add(css.toExternalForm());
    });

    final var nameOpt = dialog.showAndWait();
    if (nameOpt.isPresent()) {
      final var name = nameOpt.get();
      final var fileSystem = this.configuration.fileSystem();
      final var targetPath = fileSystem.getPath(name);

      /*
       * If the specified path is a directory, then simply set the current
       * directory to that path.
       */

      if (Files.isDirectory(targetPath)) {
        this.setCurrentDirectory(targetPath, () -> {
          this.trySelectDirectoryItem(this.directoryTable.getItems(), ".");
        });
        return;
      }

      /*
       * Otherwise, set the current directory to the parent of the target
       * path, and attempt to select the file with the path's file name.
       */

      final var targetFileNameOpt =
        Optional.ofNullable(targetPath.getFileName())
          .map(Path::toString);

      final var parent = targetPath.getParent();
      if (parent != null) {
        this.setCurrentDirectory(parent, () -> {
          targetFileNameOpt.ifPresent(
            targetName -> this.fileName.setText(targetName));
        });
      } else {
        targetFileNameOpt.ifPresent(
          targetName -> this.fileName.setText(targetName));
      }
    }
  }

  @FXML
  private void onUpDirectoryButton()
  {
    final var parent = this.currentDirectory.getParent();
    if (parent != null) {
      this.setCurrentDirectory(parent, AND_THEN_DO_NOTHING);
    }
  }

  @FXML
  private void onCreateDirectoryButton()
  {
    final var dialog = new TextInputDialog();
    dialog.setTitle(this.strings.createDirectoryTitle());
    dialog.setHeaderText(null);
    dialog.setContentText(this.strings.enterDirectoryName());

    this.configuration.cssStylesheet().ifPresent(css -> {
      dialog.getDialogPane()
        .getStylesheets()
        .add(css.toExternalForm());
    });

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
      this.setCurrentDirectory(this.currentDirectory, AND_THEN_DO_NOTHING);
    }
  }

  @FXML
  private void onOKSelected()
  {
    LOG.debug("ok: selected");

    this.result = List.of();

    final var resultTarget =
      switch (this.configuration.action()) {
        case OPEN_EXISTING_MULTIPLE, OPEN_EXISTING_SINGLE -> {
          yield this.directoryTableSelectionModel
            .getSelectedItems()
            .stream()
            .map(JWFileItem::path)
            .collect(Collectors.toList());
        }
        case CREATE -> {
          yield List.of(this.currentDirectory.resolve(this.fileName.getText()));
        }
      };

    final boolean confirmed;
    if (this.isFileSelectionConfirmationRequired(resultTarget)) {
      confirmed = this.confirmFileSelection(resultTarget);
    } else {
      confirmed = true;
    }

    if (!confirmed) {
      LOG.trace("ok: confirmation failed");
      return;
    }

    this.result = resultTarget;
    final var window =
      (Stage) this.mainContent.getScene().getWindow();

    LOG.trace("ok: hiding window");
    window.close();
  }

  /**
   * Open a dialog that asks if the user wants to replace an existing file.
   *
   * @param results The selected files
   *
   * @return {@code true} if the user confirms they want to replace files
   */

  private boolean confirmFileSelection(
    final List<Path> results)
  {
    final var shortName =
      results.stream()
        .findFirst()
        .map(Path::getFileName)
        .map(Path::toString)
        .orElse("");

    final var messageOverrides =
      this.configuration.stringOverrides();

    final var confirmButtonMessage =
      messageOverrides.confirmReplaceButton()
        .orElse(this.strings.fileConfirmReplaceButton());

    final var confirmMessage =
      messageOverrides.confirmReplaceMessage(shortName)
        .orElse(this.strings.fileConfirmReplace(shortName));

    final var confirmTitle =
      messageOverrides.confirmTitleMessage()
        .orElse(this.strings.fileConfirmReplaceTitle());

    final var confirm =
      new ButtonType(confirmButtonMessage, OK_DONE);
    final var dialog =
      new Alert(CONFIRMATION, confirmMessage);

    dialog.setHeaderText(confirmTitle);

    final var dialogButtons = dialog.getButtonTypes();
    dialogButtons.clear();
    dialogButtons.add(CANCEL);
    dialogButtons.add(confirm);

    this.configuration.cssStylesheet().ifPresent(css -> {
      dialog.getDialogPane()
        .getStylesheets()
        .add(css.toExternalForm());
    });

    return dialog.showAndWait()
      .stream()
      .anyMatch(b -> Objects.equals(b, confirm));
  }

  /**
   * File selection confirmation is only required if confirmation is enabled
   * and any of the selected files exist.
   *
   * @param files The selected files
   *
   * @return {@code true} if a confirmation dialog must be displayed
   */

  private boolean isFileSelectionConfirmationRequired(
    final List<Path> files)
  {
    return switch (this.configuration.action()) {
      case OPEN_EXISTING_SINGLE, OPEN_EXISTING_MULTIPLE -> false;
      case CREATE -> {
        if (this.configuration.confirmFileSelection()) {
          yield files.stream().anyMatch(Files::exists);
        } else {
          yield false;
        }
      }
    };

  }

  @FXML
  private void onCancelSelected()
  {
    this.result = List.of();

    final var window = this.mainContent.getScene().getWindow();
    window.hide();
  }

  @FXML
  private void onHomeSelected()
  {
    final var homeDirectoryOpt =
      this.configuration.homeDirectory();
    homeDirectoryOpt
      .ifPresent(path -> this.setCurrentDirectory(path, AND_THEN_DO_NOTHING));
  }

  @FXML
  private void onNameFieldAction()
  {
    this.trySelectDirectoryItemOrDeselect(
      this.directoryTable,
      this.fileName.getText()
    );

    this.reconfigureOKButton();
    this.okButton.requestFocus();
  }

  @FXML
  private void onNameFieldChanged()
  {
    this.trySelectDirectoryItemOrDeselect(
      this.directoryTable,
      this.fileName.getText()
    );

    this.reconfigureOKButton();
  }

  /**
   * Configure the visibility and contents of various UI buttons.
   */

  private void configureButtons()
  {
    this.configureButtonHome();

    final var messageOverrides =
      this.configuration.stringOverrides();

    switch (this.configuration.action()) {
      case OPEN_EXISTING_MULTIPLE:
      case OPEN_EXISTING_SINGLE: {
        final var buttonMessage =
          messageOverrides.buttonOpen().orElse(this.strings.open());
        this.okButton.setText(buttonMessage);
        break;
      }
      case CREATE: {
        final var buttonMessage =
          messageOverrides.buttonSave().orElse(this.strings.save());
        this.okButton.setText(buttonMessage);
        break;
      }
    }

    this.okButton.setDisable(true);

    this.newDirectoryButton.setDisable(
      !this.configuration.allowDirectoryCreation());

    this.directoryTableSelectionModel.selectedItemProperty()
      .addListener(item -> this.reconfigureOKButton());
  }

  /**
   * Show or hide the home button based on the chooser configuration.
   */

  private void configureButtonHome()
  {
    final var homeParent =
      this.homeDirectoryButton.getParent();
    final var homeDirectoryOpt =
      this.configuration.homeDirectory();

    if (homeDirectoryOpt.isEmpty()) {
      homeParent.setVisible(false);
      homeParent.setManaged(false);
    }
  }

  private void configureTableView()
  {
    switch (this.configuration.action()) {
      case OPEN_EXISTING_SINGLE:
      case CREATE:
        this.directoryTableSelectionModel
          .setSelectionMode(SelectionMode.SINGLE);
        break;
      case OPEN_EXISTING_MULTIPLE:
        this.directoryTableSelectionModel
          .setSelectionMode(SelectionMode.MULTIPLE);
        break;
    }

    this.directoryTable.setPlaceholder(new Label(""));

    final var tableColumns =
      this.directoryTable.getColumns();
    final var tableTypeColumn =
      (TableColumn<JWFileItem, JWFileItem>) tableColumns.get(0);
    final var tableNameColumn =
      (TableColumn<JWFileItem, JWFileItem>) tableColumns.get(1);
    final var tableSizeColumn =
      (TableColumn<JWFileItem, Long>) tableColumns.get(2);
    final var tableTimeColumn =
      (TableColumn<JWFileItem, FileTime>) tableColumns.get(3);

    tableTypeColumn.setSortable(true);
    tableTypeColumn.setReorderable(false);
    tableTypeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, JWFileItem> cell =
        new JWFileItemTableTypeCell(this.imageSet, this.toolTips);
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableNameColumn.setSortable(true);
    tableNameColumn.setReorderable(false);
    tableNameColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, JWFileItem> cell =
        new JWFileItemTableNameCell(this.toolTips);
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableSizeColumn.setSortable(true);
    tableSizeColumn.setReorderable(false);
    tableSizeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, Long> cell =
        new JWFileItemTableSizeCell(this::formatSize);
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableTimeColumn.setSortable(true);
    tableTimeColumn.setReorderable(false);
    tableTimeColumn.setCellFactory(column -> {
      final TableCell<JWFileItem, FileTime> cell =
        new JWFileItemTableTimeCell(this.configuration.fileTimeFormatter());
      cell.setOnMouseClicked(this::onTableRowClicked);
      return cell;
    });

    tableTypeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableNameColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableSizeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(Long.valueOf(param.getValue().size())));
    tableTimeColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue().modifiedTime()));

    this.fileListing.comparator()
      .bind(this.directoryTable.comparatorProperty());
  }

  private void reconfigureOKButton()
  {
    var enabled = false;
    switch (this.configuration.action()) {
      case OPEN_EXISTING_MULTIPLE:
      case OPEN_EXISTING_SINGLE: {
        enabled = this.atLeastOneItemSelected();
        break;
      }
      case CREATE: {
        enabled = this.atLeastOneItemSelected() || this.fileNameNotEmpty();
        break;
      }
    }

    this.okButton.setDisable(!enabled);
  }

  private boolean fileNameNotEmpty()
  {
    return !this.fileName.getText().isEmpty();
  }

  /**
   * Answers whether the given {@link Path} may be returned to the client.
   *
   * @param path A file selected in the user interface.
   *
   * @return {@code true} if the given {@link Path} is an acceptable selection.
   */

  private boolean filterSelectionMode(final Path path)
  {
    return this.configuration.fileSelectionMode().apply(path);
  }

  private boolean atLeastOneItemSelected()
  {
    final var selectedItems =
      this.directoryTableSelectionModel
        .getSelectedItems();

    for (final var selectedItem : selectedItems) {
      final var itemPath = selectedItem.path();
      if (this.filterSelectionMode(itemPath)) {
        return true;
      }
    }

    return false;
  }

  private void onTableRowClicked(
    final MouseEvent event)
  {
    if (event.getClickCount() == 1) {
      final var selectionModel = this.directoryTableSelectionModel;
      final var item = selectionModel.getSelectedItem();
      if (item != null) {
        this.fileName.setText(item.name());
      }
      return;
    }

    if (event.getClickCount() == 2) {
      final var selectionModel = this.directoryTableSelectionModel;
      final var item = selectionModel.getSelectedItem();
      if (item != null) {
        final var directory = item.path();
        if (Files.isDirectory(directory)) {
          this.setCurrentDirectory(directory, AND_THEN_DO_NOTHING);
        }
      }
    }
  }

  private void onSourceItemDoubleClicked(
    final MouseEvent event)
  {
    if (event.getClickCount() == 2) {
      final var item =
        this.sourcesList.getSelectionModel().getSelectedItem();
      item.path()
        .ifPresent(path -> this.setCurrentDirectory(path, AND_THEN_DO_NOTHING));
      this.populateDirectoryTableWith(item, AND_THEN_DO_NOTHING);
    }
  }

  /**
   * @return The list of selected files, if any
   */

  public List<Path> result()
  {
    return this.result;
  }

  /**
   * Cancel and hide the file selection.
   */

  public void cancel()
  {
    this.result = List.of();
    this.mainContent.getScene()
      .getWindow()
      .hide();
  }

  private final class SourceListCellFactory
    implements Callback<ListView<JWFileSourceEntryType>, ListCell<JWFileSourceEntryType>>
  {
    SourceListCellFactory()
    {

    }

    @Override
    public ListCell<JWFileSourceEntryType> call(
      final ListView<JWFileSourceEntryType> param)
    {
      final ListCell<JWFileSourceEntryType> cell =
        new JWFileSourceEntryTypeListCell();
      cell.setOnMouseClicked(
        JWFileChooserViewController.this::onSourceItemDoubleClicked);
      return cell;
    }
  }

  private final class JWFileSourceEntryTypeListCell
    extends ListCell<JWFileSourceEntryType>
  {
    JWFileSourceEntryTypeListCell()
    {

    }

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
        JWFileChooserViewController.this.strings,
        this
      );
    }
  }

}
