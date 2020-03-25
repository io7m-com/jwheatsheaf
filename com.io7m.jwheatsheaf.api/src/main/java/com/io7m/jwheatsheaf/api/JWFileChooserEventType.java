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
import org.immutables.value.Value;

import java.nio.file.Path;

/**
 * The type of events published during the process of choosing files.
 */

public interface JWFileChooserEventType
{
  /**
   * The type of events that correspond to errors.
   */

  interface JWFileChooserEventErrorType extends JWFileChooserEventType
  {
    /**
     * @return The path referred to by the error
     */

    Path path();

    /**
     * @return The exception
     */

    Exception exception();
  }

  /**
   * An error occurred whilst trying to list a directory.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface JWFileListingFailedType extends JWFileChooserEventErrorType
  {
    /**
     * @return The directory that could not be listed
     */

    @Override
    @Value.Parameter
    Path path();

    /**
     * @return The exception
     */

    @Override
    @Value.Parameter
    Exception exception();
  }

  /**
   * An error occurred whilst trying to create a directory.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface JWDirectoryCreationFailedType extends JWFileChooserEventErrorType
  {
    /**
     * @return The directory that could not be created
     */

    @Override
    @Value.Parameter
    Path path();

    /**
     * @return The exception
     */

    @Override
    @Value.Parameter
    Exception exception();
  }
}
