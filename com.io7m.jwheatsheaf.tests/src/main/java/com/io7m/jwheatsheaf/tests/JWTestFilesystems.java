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

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import com.github.marschall.memoryfilesystem.MemoryFileSystemProperties;
import com.github.marschall.memoryfilesystem.StringTransformer;
import com.github.marschall.memoryfilesystem.StringTransformers;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JWTestFilesystems
{
  private final Map<String, FileSystem> filesystems;

  private JWTestFilesystems(
    final Map<String, FileSystem> inFilesystems)
  {
    this.filesystems =
      Objects.requireNonNull(inFilesystems, "filesystems");
  }

  public static JWTestFilesystems create()
    throws IOException
  {
    final var filesystems =
      Map.ofEntries(
        Map.entry("ExampleDOS", createDosFilesystem()),
        Map.entry("Default", FileSystems.getDefault()),
        Map.entry("Broken", createBrokenFilesystem()),
        Map.entry("BrokenFiles", createBrokenFilesFilesystem())
      );

    return new JWTestFilesystems(filesystems);
  }

  private static FileSystem createBrokenFilesystem()
    throws IOException
  {
    final var provider =
      Mockito.mock(FileSystemProvider.class);
    final var filesystem =
      Mockito.mock(FileSystem.class);
    final var path =
      Mockito.mock(Path.class);
    final var attributes =
      Mockito.mock(BasicFileAttributes.class);

    Mockito.when(attributes.lastModifiedTime())
      .thenReturn(FileTime.fromMillis(0L));

    Mockito.when(filesystem.provider())
      .thenReturn(provider);
    Mockito.when(path.getFileSystem())
      .thenReturn(filesystem);
    Mockito.when(filesystem.getPath(Mockito.anyString()))
      .thenReturn(path);

    Mockito.when(provider.readAttributes(
        path,
        BasicFileAttributes.class,
        LinkOption.NOFOLLOW_LINKS))
      .thenReturn(attributes);
    Mockito.when(provider.readAttributes(path, BasicFileAttributes.class))
      .thenReturn(attributes);
    Mockito.when(provider.newDirectoryStream(Mockito.any(), Mockito.any()))
      .thenThrow(new IOException());

    return filesystem;
  }

  private static FileSystem createBrokenFilesFilesystem()
    throws IOException
  {
    final var provider =
      Mockito.mock(FileSystemProvider.class);
    final var filesystem =
      Mockito.mock(FileSystem.class);
    final var path =
      Mockito.mock(Path.class);
    final var attributes =
      Mockito.mock(BasicFileAttributes.class);
    final var directoryStream =
      Mockito.mock(DirectoryStream.class);

    Mockito.when(directoryStream.iterator())
      .thenReturn(List.of(path).iterator());

    Mockito.when(attributes.lastModifiedTime())
      .thenReturn(FileTime.fromMillis(0L));

    Mockito.when(filesystem.provider())
      .thenReturn(provider);
    Mockito.when(path.getFileSystem())
      .thenReturn(filesystem);
    Mockito.when(filesystem.getPath(Mockito.anyString()))
      .thenReturn(path);

    Mockito.when(provider.readAttributes(
        path,
        BasicFileAttributes.class,
        LinkOption.NOFOLLOW_LINKS))
      .thenReturn(attributes);
    Mockito.when(provider.readAttributes(path, BasicFileAttributes.class))
      .thenReturn(attributes);
    Mockito.when(provider.newDirectoryStream(Mockito.any(), Mockito.any()))
      .thenReturn(directoryStream);

    return filesystem;
  }

  private static FileSystem createDosFilesystem()
    throws IOException
  {
    final var filesystem =
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

    final var home = filesystem.getPath("Z:\\HOME");
    Files.createDirectories(home);
    Files.writeString(filesystem.getPath("Z:\\HOME", "FILE.TXT"), "FILE!");

    final var doc = filesystem.getPath("DOC");
    Files.createDirectories(doc);
    Files.setLastModifiedTime(doc, fileTimeOfYear(2002));

    final var readme = filesystem.getPath("README.TXT");
    Files.writeString(readme, "HELLO!");
    Files.setLastModifiedTime(readme, fileTimeOfYear(2003));

    final var data = filesystem.getPath("DATA.XML");
    Files.writeString(data, "Some data.");
    Files.setLastModifiedTime(data, fileTimeOfYear(2004));

    final var photo = filesystem.getPath("PHOTO.JPG");
    Files.writeString(photo, "☺");
    Files.setLastModifiedTime(photo, fileTimeOfYear(2005));

    final var dot = filesystem.getPath(".");
    Files.setLastModifiedTime(dot, fileTimeOfYear(2001));
    return filesystem;
  }

  private static FileTime fileTimeOfYear(
    final int year)
  {
    return FileTime.from(
      Instant.parse(
        "%d-01-01T00:00:00+00:00".formatted(Integer.valueOf(year))
      )
    );
  }

  public Map<String, FileSystem> filesystems()
  {
    return this.filesystems;
  }
}
