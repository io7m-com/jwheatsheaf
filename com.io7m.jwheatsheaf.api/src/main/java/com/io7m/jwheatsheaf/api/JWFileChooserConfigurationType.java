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

package com.io7m.jwheatsheaf.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jaffirm.core.Preconditions;
import org.immutables.value.Value;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The configuration information used to instantiate file choosers.
 *
 * File choosers have a wide range of configurable properties. The only
 * mandatory configuration parameter is the {@link #fileSystem()}, because
 * this defines the initial filesystem used for file selection.
 */

@ImmutablesStyleType
@Value.Immutable
public interface JWFileChooserConfigurationType
{
  /**
   * @return The list of recent files to be shown in the file chooser
   */

  List<Path> recentFiles();

  /**
   * @return The file system traversed by the file chooser
   */

  @Value.Default
  default FileSystem fileSystem()
  {
    return FileSystems.getDefault();
  }

  /**
   * @return The starting directory
   */

  Optional<Path> initialDirectory();

  /**
   * The directory to which the chooser will navigate when the user clicks
   * the "home" button. If no path is specified, the button is not shown.
   *
   * @return The user's home directory
   */

  Optional<Path> homeDirectory();

  /**
   * @return The file name initially entered into the selection field
   */

  Optional<String> initialFileName();

  /**
   * @return A string that will override the generic dialog title
   */

  Optional<String> title();

  /**
   * The image set used to select images for the file chooser UI. If no
   * set is specified here, a default set of images and icons will be used.
   *
   * @return An image set
   */

  Optional<JWFileImageSetType> fileImageSet();

  /**
   * The CSS stylesheet that will be added to the file chooser UI.
   *
   * @return The URL of the CSS stylesheet
   */

  Optional<URL> cssStylesheet();

  /**
   * @return The list of file filters
   */

  List<JWFileChooserFilterType> fileFilters();

  /**
   * @return The default file filter
   */

  Optional<JWFileChooserFilterType> fileFilterDefault();

  /**
   * Sets a mode that prevents returning a file to the client if that file
   * does not meet criteria returned by this method. For example, this may be
   * used to prevent selecting directories by implementing a function that
   * returns {@code true} when the file is considered a regular file.
   *
   * @return The {@link Function} that accepts or rejects selected files
   */

  @Value.Default
  default Function<Path, Boolean> fileSelectionMode()
  {
    return JWFileChooserConfigurationDefaults.fileSelectionMode();
  }

  /**
   * @return {@code true} if the UI will allow the creation of directories
   */

  @Value.Default
  default boolean allowDirectoryCreation()
  {
    return true;
  }

  /**
   * Determine whether or not to show a link to the parent directory inside
   * the file listing. This entry is always called "..".
   *
   * @return {@code true} if the directory listing will contain ".."
   *
   * @since 3.0.0
   */

  @Value.Default
  default boolean showParentDirectory()
  {
    return false;
  }

  /**
   * @return The date/time formatter used to display file times
   */

  @Value.Default
  default DateTimeFormatter fileTimeFormatter()
  {
    return JWFileChooserConfigurationDefaults.fileTimeFormatter();
  }

  /**
   * @return The formatter used to display file sizes
   */

  @Value.Default
  default JWFileSizeFormatterType fileSizeFormatter()
  {
    return JWFileChooserConfigurationDefaults.fileSizeFormatter();
  }

  /**
   * @return The action that the user is performing
   */

  @Value.Default
  default JWFileChooserAction action()
  {
    return JWFileChooserAction.OPEN_EXISTING_SINGLE;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    this.initialDirectory().ifPresent(path -> {
      Preconditions.checkPreconditionV(
        path.getFileSystem().equals(this.fileSystem()),
        "Path %s must belong to filesystem %s",
        path,
        this.fileSystem()
      );
    });

    for (final var path : this.recentFiles()) {
      Preconditions.checkPreconditionV(
        path.getFileSystem().equals(this.fileSystem()),
        "Path %s must belong to filesystem %s",
        path,
        this.fileSystem()
      );
    }

    final var filterOpt = this.fileFilterDefault();
    filterOpt.ifPresent(filter -> {
      Preconditions.checkPreconditionV(
        this.fileFilters().contains(filter),
        "The default file filter must be contained in the list of filters"
      );
    });
  }

  /**
   * If set to {@code true}, then when the user is using a mode such as
   * {@link JWFileChooserAction#CREATE}, a confirmation dialog will be displayed
   * if the user selects a file that already exists.
   *
   * @return {@code true} if confirmation dialogs should be shown
   *
   * @since 3.0.0
   */

  @Value.Default
  default boolean confirmFileSelection()
  {
    return false;
  }

  /**
   * @return A provider of UI string overrides
   *
   * @since 3.0.0
   */

  @Value.Default
  default JWFileChooserStringOverridesType stringOverrides()
  {
    return JWFileChooserStringOverridesEmpty.get();
  }
}
