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

import com.io7m.jwheatsheaf.api.JWFileKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Functions over file items.
 */

public final class JWFileItems
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileItems.class);

  private JWFileItems()
  {

  }

  /**
   * List the given directory, resolving each entry into a file item.
   *
   * @param directory The directory
   * @param withParent {@code true} if the parent directory entry should be included
   *
   * @return A list of items
   *
   * @throws IOException On I/O errors
   */

  public static List<JWFileItem> listDirectory(
    final Path directory,
    final boolean withParent)
    throws IOException
  {
    final var items = new ArrayList<JWFileItem>(32);
    items.add(resolveFileItem(directory).withDisplayName("."));

    if (withParent) {
      final var directoryParent = directory.getParent();
      if (directoryParent != null) {
        items.add(resolveFileItem(directoryParent).withDisplayName(".."));
      }
    }

    try (var stream = Files.list(directory)) {
      stream.sorted().forEach(path -> items.add(resolveFileItem(path)));
    }
    return items;
  }

  /**
   * Resolve the given path into a file item. If the file item cannot be
   * resolved due to permissions errors or other I/O errors, a file item
   * is returned with identity values in each field (such as 0 for the file
   * size).
   *
   * @param path The input path
   *
   * @return A file item
   */

  public static JWFileItem resolveFileItem(
    final Path path)
  {
    Objects.requireNonNull(path, "path");

    try {
      return JWFileItem.builder()
        .setKind(fileKind(path))
        .setModifiedTime(fileTime(path))
        .setSize(fileSize(path))
        .setPath(path)
        .build();
    } catch (final IOException e) {
      LOG.error("i/o exception during directory listing: ", e);
      return JWFileItem.builder()
        .setKind(JWFileKind.UNKNOWN)
        .setModifiedTime(FileTime.fromMillis(0L))
        .setSize(0L)
        .setPath(path)
        .build();
    }
  }

  private static long fileSize(
    final Path path)
    throws IOException
  {
    return Files.size(path);
  }

  private static FileTime fileTime(
    final Path path)
    throws IOException
  {
    return Files.getLastModifiedTime(path);
  }

  private static JWFileKind fileKind(
    final Path path)
  {
    if (Files.isSymbolicLink(path)) {
      return JWFileKind.SYMBOLIC_LINK;
    }
    if (Files.isDirectory(path)) {
      return JWFileKind.DIRECTORY;
    }
    if (Files.isRegularFile(path)) {
      return JWFileKind.REGULAR_FILE;
    }
    return JWFileKind.UNKNOWN;
  }
}
