/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.jwheatsheaf.filter.glob.JWFilterGlobFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.io7m.jwheatsheaf.filter.glob.JWFilterGlobRuleKind.EXCLUDE;
import static com.io7m.jwheatsheaf.filter.glob.JWFilterGlobRuleKind.EXCLUDE_AND_HALT;
import static com.io7m.jwheatsheaf.filter.glob.JWFilterGlobRuleKind.INCLUDE;
import static com.io7m.jwheatsheaf.filter.glob.JWFilterGlobRuleKind.INCLUDE_AND_HALT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class JWFilterGlobTest
{
  private JWFilterGlobFactory filters;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.filters = new JWFilterGlobFactory();
    this.directory = JWTestDirectories.createTempDirectory();
  }

  /**
   * A filter with no rules excludes everything but directories.
   *
   * @throws Exception On errors
   */

  @Test
  public void testEmptyExcludesAll()
    throws Exception
  {
    final var file = this.directory.resolve("file.txt");
    Files.writeString(file, "Hello!");

    final var filter =
      this.filters.create("Allow nothing!")
        .build();

    assertEquals("Allow nothing!", filter.description());
    assertTrue(filter.isAllowed(this.directory));
    assertFalse(filter.isAllowed(file));
  }

  /**
   * A filter includes text files correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testIncludesText()
    throws Exception
  {
    final var file0 = this.directory.resolve("file.txt");
    Files.writeString(file0, "Hello!");
    final var file1 = this.directory.resolve("file.png");
    Files.writeString(file1, "Hello!");

    final var filter =
      this.filters.create("Example")
        .addRule(INCLUDE, "**/*.txt")
        .build();

    assertEquals("Example", filter.description());
    assertTrue(filter.isAllowed(this.directory));
    assertTrue(filter.isAllowed(file0));
    assertFalse(filter.isAllowed(file1));
  }

  /**
   * A filter includes text files correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testIncludesTextHalt()
    throws Exception
  {
    final var file0 = this.directory.resolve("file.txt");
    Files.writeString(file0, "Hello!");
    final var file1 = this.directory.resolve("file.png");
    Files.writeString(file1, "Hello!");

    final var filter =
      this.filters.create("Example")
        .addRule(INCLUDE_AND_HALT, "**/*.txt")
        .build();

    assertEquals("Example", filter.description());
    assertTrue(filter.isAllowed(this.directory));
    assertTrue(filter.isAllowed(file0));
    assertFalse(filter.isAllowed(file1));
  }

  /**
   * A filter excludes text files correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testExcludesText()
    throws Exception
  {
    final var file0 = this.directory.resolve("file.txt");
    Files.writeString(file0, "Hello!");
    final var file1 = this.directory.resolve("file.png");
    Files.writeString(file1, "Hello!");

    final var filter =
      this.filters.create("Example")
        .addRule(INCLUDE, "**/*")
        .addRule(EXCLUDE, "**/*.txt")
        .build();

    assertEquals("Example", filter.description());
    assertTrue(filter.isAllowed(this.directory));
    assertFalse(filter.isAllowed(file0));
    assertTrue(filter.isAllowed(file1));
  }

  /**
   * A filter excludes text files correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testExcludesTextHalt()
    throws Exception
  {
    final var file0 = this.directory.resolve("file.txt");
    Files.writeString(file0, "Hello!");
    final var file1 = this.directory.resolve("file.png");
    Files.writeString(file1, "Hello!");

    final var filter =
      this.filters.create("Example")
        .addRule(INCLUDE, "**/*")
        .addRule(EXCLUDE_AND_HALT, "**/*.txt")
        .build();

    assertEquals("Example", filter.description());
    assertTrue(filter.isAllowed(this.directory));
    assertFalse(filter.isAllowed(file0));
    assertTrue(filter.isAllowed(file1));
  }
}
