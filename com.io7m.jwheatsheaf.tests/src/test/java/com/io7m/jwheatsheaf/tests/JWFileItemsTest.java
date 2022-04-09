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

package com.io7m.jwheatsheaf.tests;

import com.io7m.jwheatsheaf.api.JWFileKind;
import com.io7m.jwheatsheaf.ui.internal.JWFileItems;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

public final class JWFileItemsTest
{
  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
  private FileSystem brokenFilesystem;
  private FileSystem brokenFilesFilesystem;

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");
    this.brokenFilesystem = systems.get("Broken");
    this.brokenFilesFilesystem = systems.get("BrokenFiles");
  }

  @Test
  public void testListDOS()
    throws IOException
  {
    final var items =
      JWFileItems.listDirectory(
        this.dosFilesystem.getPath(""), false);

    Assertions.assertEquals(5, items.size());

    Assertions.assertEquals(".", items.get(0).name());
    Assertions.assertEquals(JWFileKind.DIRECTORY, items.get(0).kind());

    Assertions.assertEquals("DATA.XML", items.get(1).name());
    Assertions.assertEquals(JWFileKind.REGULAR_FILE, items.get(1).kind());

    Assertions.assertEquals("DOC", items.get(2).name());
    Assertions.assertEquals(JWFileKind.DIRECTORY, items.get(2).kind());

    Assertions.assertEquals("PHOTO.JPG", items.get(3).name());
    Assertions.assertEquals(JWFileKind.REGULAR_FILE, items.get(3).kind());

    Assertions.assertEquals("README.TXT", items.get(4).name());
    Assertions.assertEquals(JWFileKind.REGULAR_FILE, items.get(4).kind());
  }

  @Test
  public void testListBroken()
    throws IOException
  {
    Assertions.assertThrows(IOException.class, () -> {
      JWFileItems.listDirectory(
        this.brokenFilesystem.getPath(""), false);
    });
  }

  @Test
  public void testListBrokenFiles()
    throws IOException
  {
    final var items =
      JWFileItems.listDirectory(
        this.brokenFilesFilesystem.getPath(""), false);

    Assertions.assertEquals(2, items.size());
    Assertions.assertEquals(JWFileKind.UNKNOWN, items.get(0).kind());
    Assertions.assertEquals(JWFileKind.UNKNOWN, items.get(1).kind());
  }
}
