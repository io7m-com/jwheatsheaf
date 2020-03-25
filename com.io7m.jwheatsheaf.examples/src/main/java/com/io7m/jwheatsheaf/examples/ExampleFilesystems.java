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

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import com.github.marschall.memoryfilesystem.MemoryFileSystemProperties;
import com.github.marschall.memoryfilesystem.StringTransformer;
import com.github.marschall.memoryfilesystem.StringTransformers;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Map;
import java.util.Objects;

public final class ExampleFilesystems
{
  private final Map<String, FileSystem> filesystems;

  private ExampleFilesystems(
    final Map<String, FileSystem> inFilesystems)
  {
    this.filesystems =
      Objects.requireNonNull(inFilesystems, "filesystems");
  }

  public static ExampleFilesystems create()
    throws IOException
  {
    final var filesystems =
      Map.ofEntries(
        Map.entry("ExampleDOS", createDosFilesystem()),
        Map.entry("Default", FileSystems.getDefault())
      );

    return new ExampleFilesystems(filesystems);
  }

  private static FileSystem createDosFilesystem()
    throws IOException
  {
    final FileSystem filesystem =
      MemoryFileSystemBuilder.newEmpty()
        .addRoot("Z:\\")
        .setSeparator("\\")
        .addUser("GROUCH")
        .addGroup("SYSTEM")
        .addFileAttributeView(DosFileAttributeView.class)
        .setCurrentWorkingDirectory("Z:\\USERS\\GROUCH")
        .setStoreTransformer(StringTransformers.IDENTIY)
        .setCaseSensitive(false)
        .setFileTimeResolution(MemoryFileSystemProperties.WINDOWS_RESOLUTION)
        .addForbiddenCharacter('\\')
        .addForbiddenCharacter('/')
        .addForbiddenCharacter(':')
        .addForbiddenCharacter('*')
        .addForbiddenCharacter('?')
        .addForbiddenCharacter('"')
        .addForbiddenCharacter('<')
        .addForbiddenCharacter('>')
        .addForbiddenCharacter('|')
        .addRoot("Y:\\")
        .addRoot("X:\\")
        .setCaseSensitive(false)
        .setStoreTransformer(new StringTransformer()
        {
          @Override
          public String transform(final String s)
          {
            return s.toUpperCase();
          }

          @Override
          public int getRegexFlags()
          {
            return 0;
          }
        })
        .build();

    Files.createDirectories(filesystem.getPath("DOC"));
    Files.writeString(filesystem.getPath("README.TXT"), "HELLO!");
    Files.writeString(filesystem.getPath("DATA.XML"), "Some data.");
    Files.writeString(filesystem.getPath("PHOTO.JPG"), "☺");
    return filesystem;
  }

  public Map<String, FileSystem> filesystems()
  {
    return this.filesystems;
  }
}
