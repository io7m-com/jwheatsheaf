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

package com.io7m.jwheatsheaf.ui.internal;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jwheatsheaf.api.JWFileKind;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

/**
 * A resolved file item.
 */

@ImmutablesStyleType
@Value.Immutable
public interface JWFileItemType
{
  /**
   * @return The file kind
   */

  JWFileKind kind();

  /**
   * @return The file path
   */

  Path path();

  /**
   * @return The file size
   */

  long size();

  /**
   * @return The file modification time
   */

  FileTime modifiedTime();

  /**
   * @return The file display name override, if any
   */

  Optional<String> displayName();

  /**
   * @return The display name, taking into account any present override
   */

  default String name()
  {
    final var displayNameOpt = this.displayName();
    return displayNameOpt.orElseGet(() -> this.path().getFileName().toString());
  }
}
