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
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

  FileSystem fileSystem();

  /**
   * @return The starting directory
   */

  Optional<Path> initialDirectory();

  /**
   * @return The file name initially entered into the selection field
   */

  Optional<String> initialFileName();

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
   * @return {@code true} if the UI will allow the creation of directories
   */

  @Value.Default
  default boolean allowDirectoryCreation()
  {
    return true;
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
  }
}
