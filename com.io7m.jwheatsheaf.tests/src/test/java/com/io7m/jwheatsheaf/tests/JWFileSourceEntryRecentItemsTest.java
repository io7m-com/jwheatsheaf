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

package com.io7m.jwheatsheaf.tests;

import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.ui.internal.JWFileSourceEntryRecentItems;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;

public final class JWFileSourceEntryRecentItemsTest
{
  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");
  }

  @Test
  public void testEmpty()
  {
    final var config =
      JWFileChooserConfiguration.builder()
        .setFileSystem(this.dosFilesystem)
        .build();

    final var source = new JWFileSourceEntryRecentItems(config);
    final var items = source.onFileItemsRequested();
    Assertions.assertEquals(Optional.empty(), source.path());
    Assertions.assertEquals(0, items.size());
  }

  @Test
  public void test3()
  {
    final var config =
      JWFileChooserConfiguration.builder()
        .setFileSystem(this.dosFilesystem)
        .addRecentFiles(this.dosFilesystem.getPath("Y:\\y"))
        .addRecentFiles(this.dosFilesystem.getPath("Y:\\x"))
        .addRecentFiles(this.dosFilesystem.getPath("Y:\\z"))
        .build();

    final var source = new JWFileSourceEntryRecentItems(config);
    final var items = source.onFileItemsRequested();
    Assertions.assertEquals(3, items.size());
    Assertions.assertEquals(
      this.dosFilesystem.getPath("Y:\\x"), items.get(0).path());
    Assertions.assertEquals(
      this.dosFilesystem.getPath("Y:\\y"), items.get(1).path());
    Assertions.assertEquals(
      this.dosFilesystem.getPath("Y:\\z"), items.get(2).path());
  }
}
